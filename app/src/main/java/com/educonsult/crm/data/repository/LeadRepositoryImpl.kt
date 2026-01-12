package com.educonsult.crm.data.repository

import com.educonsult.crm.data.local.datastore.UserPreferences
import com.educonsult.crm.data.local.db.dao.LeadDao
import com.educonsult.crm.data.local.db.dao.LeadNoteDao
import com.educonsult.crm.data.local.db.dao.LeadStatusDao
import com.educonsult.crm.data.mapper.toDomain
import com.educonsult.crm.data.mapper.toEntity
import com.educonsult.crm.data.mapper.toSaveRequest
import com.educonsult.crm.data.remote.api.LeadApi
import com.educonsult.crm.data.remote.dto.lead.request.GetLeadsRequest
import com.educonsult.crm.data.remote.dto.lead.request.GetNotesRequest
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.model.LeadStatus
import com.educonsult.crm.domain.repository.LeadFilter
import com.educonsult.crm.domain.repository.LeadRepository
import com.educonsult.crm.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadRepositoryImpl @Inject constructor(
    private val leadApi: LeadApi,
    private val leadDao: LeadDao,
    private val leadNoteDao: LeadNoteDao,
    private val leadStatusDao: LeadStatusDao,
    private val userPreferences: UserPreferences,
    private val dispatcherProvider: DispatcherProvider
) : LeadRepository {

    companion object {
        private const val SYNC_STATUS_SYNCED = 0
        private const val SYNC_STATUS_CREATED = 1
        private const val SYNC_STATUS_UPDATED = 2
        private const val SYNC_STATUS_DELETED = 3
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getLeads(filter: LeadFilter?): Flow<List<Lead>> {
        return when {
            filter?.searchQuery != null -> leadDao.searchLeads(filter.searchQuery)
            filter?.statusId != null -> leadDao.getByStatus(filter.statusId)
            filter?.priority != null -> leadDao.getByPriority(filter.priority.name.lowercase())
            filter?.assignedTo != null -> leadDao.getByAssignee(filter.assignedTo)
            else -> leadDao.getAll()
        }.map { entities ->
            val statusMap = getStatusMap()
            entities.map { entity -> entity.toDomain(statusMap[entity.statusId]) }
        }.flowOn(dispatcherProvider.io)
    }

    override suspend fun getLeadById(id: String): Lead? {
        return withContext(dispatcherProvider.io) {
            val entity = leadDao.getById(id) ?: return@withContext null
            val status = entity.statusId?.let { leadStatusDao.getById(it) }
            entity.toDomain(status)
        }
    }

    override suspend fun getLeadByPhone(phone: String): Lead? {
        return withContext(dispatcherProvider.io) {
            val entity = leadDao.getByPhone(phone) ?: return@withContext null
            val status = entity.statusId?.let { leadStatusDao.getById(it) }
            entity.toDomain(status)
        }
    }

    override suspend fun saveLead(lead: Lead): Result<Lead> {
        return withContext(dispatcherProvider.io) {
            try {
                val isNew = lead.id.isBlank() || lead.id.startsWith("local_")
                val leadToSave = if (isNew) {
                    lead.copy(
                        id = "local_${UUID.randomUUID()}",
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                } else {
                    lead.copy(updatedAt = LocalDateTime.now())
                }

                val syncStatus = if (isNew) SYNC_STATUS_CREATED else SYNC_STATUS_UPDATED
                leadDao.insert(leadToSave.toEntity(syncStatus))

                try {
                    val response = leadApi.saveLead(leadToSave.toSaveRequest())
                    if (response.isSuccessful && response.body()?.status == true) {
                        val savedLead = response.body()?.data?.lead
                        if (savedLead != null) {
                            if (isNew && leadToSave.id != savedLead.id) {
                                leadDao.delete(leadToSave.toEntity())
                            }
                            leadDao.insert(savedLead.toEntity())
                            return@withContext Result.success(savedLead.toDomain())
                        }
                    }
                } catch (e: Exception) {
                    // Network error, keep local copy with pending sync
                }

                Result.success(leadToSave)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteLead(id: String): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                leadDao.softDelete(id, System.currentTimeMillis())

                try {
                    val response = leadApi.deleteLead(mapOf("id" to id))
                    if (response.isSuccessful && response.body()?.status == true) {
                        leadDao.getById(id)?.let { leadDao.delete(it) }
                    }
                } catch (e: Exception) {
                    // Network error, will sync later
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun syncLeads(): Result<Int> {
        return withContext(dispatcherProvider.io) {
            try {
                var syncedCount = 0

                val pendingLeads = leadDao.getPendingSync()
                for (lead in pendingLeads) {
                    try {
                        when (lead.syncStatus) {
                            SYNC_STATUS_CREATED, SYNC_STATUS_UPDATED -> {
                                val status = lead.statusId?.let { leadStatusDao.getById(it) }
                                val response = leadApi.saveLead(lead.toDomain(status).toSaveRequest())
                                if (response.isSuccessful && response.body()?.status == true) {
                                    val savedLead = response.body()?.data?.lead
                                    if (savedLead != null) {
                                        if (lead.id != savedLead.id) {
                                            leadDao.delete(lead)
                                        }
                                        leadDao.insert(savedLead.toEntity())
                                        syncedCount++
                                    }
                                }
                            }
                            SYNC_STATUS_DELETED -> {
                                val response = leadApi.deleteLead(mapOf("id" to lead.id))
                                if (response.isSuccessful && response.body()?.status == true) {
                                    leadDao.delete(lead)
                                    syncedCount++
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with next item
                    }
                }

                val pendingNotes = leadNoteDao.getPendingSync()
                for (note in pendingNotes) {
                    try {
                        if (note.syncStatus == SYNC_STATUS_CREATED || note.syncStatus == SYNC_STATUS_UPDATED) {
                            val response = leadApi.saveNote(note.toDomain().toSaveRequest())
                            if (response.isSuccessful && response.body()?.status == true) {
                                val savedNote = response.body()?.data?.note
                                if (savedNote != null) {
                                    if (note.id != savedNote.id) {
                                        leadNoteDao.delete(note)
                                    }
                                    leadNoteDao.insert(savedNote.toEntity())
                                    syncedCount++
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with next item
                    }
                }

                try {
                    val response = leadApi.getLeads(GetLeadsRequest())
                    if (response.isSuccessful && response.body()?.status == true) {
                        val leads = response.body()?.data?.leads ?: emptyList()
                        leadDao.insertAll(leads.map { it.toEntity() })
                        syncedCount += leads.size
                    }
                } catch (e: Exception) {
                    // Failed to fetch from server
                }

                try {
                    val statusResponse = leadApi.getStatuses()
                    if (statusResponse.isSuccessful && statusResponse.body()?.status == true) {
                        val statuses = statusResponse.body()?.data ?: emptyList()
                        leadStatusDao.insertAll(statuses.map { it.toEntity() })
                    }
                } catch (e: Exception) {
                    // Failed to fetch statuses
                }

                Result.success(syncedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getLeadStatuses(): Flow<List<LeadStatus>> {
        return leadStatusDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(dispatcherProvider.io)
    }

    override fun getFollowUpsDue(): Flow<List<Lead>> {
        val now = System.currentTimeMillis()
        return leadDao.getFollowUpsDue(now).map { entities ->
            val statusMap = getStatusMap()
            entities.map { entity -> entity.toDomain(statusMap[entity.statusId]) }
        }.flowOn(dispatcherProvider.io)
    }

    override suspend fun saveNote(leadId: String, note: LeadNote): Result<LeadNote> {
        return withContext(dispatcherProvider.io) {
            try {
                val userId = userPreferences.userId.first() ?: ""
                val isNew = note.id.isBlank() || note.id.startsWith("local_")
                val noteToSave = if (isNew) {
                    note.copy(
                        id = "local_${UUID.randomUUID()}",
                        leadId = leadId,
                        createdBy = userId,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now()
                    )
                } else {
                    note.copy(updatedAt = LocalDateTime.now())
                }

                val syncStatus = if (isNew) SYNC_STATUS_CREATED else SYNC_STATUS_UPDATED
                leadNoteDao.insert(noteToSave.toEntity(syncStatus))

                if (isNew) {
                    leadDao.incrementNoteCount(leadId, System.currentTimeMillis())
                }

                try {
                    val response = leadApi.saveNote(noteToSave.toSaveRequest())
                    if (response.isSuccessful && response.body()?.status == true) {
                        val savedNote = response.body()?.data?.note
                        if (savedNote != null) {
                            if (isNew && noteToSave.id != savedNote.id) {
                                leadNoteDao.delete(noteToSave.toEntity())
                            }
                            leadNoteDao.insert(savedNote.toEntity())
                            return@withContext Result.success(savedNote.toDomain())
                        }
                    }
                } catch (e: Exception) {
                    // Network error, keep local copy
                }

                Result.success(noteToSave)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun getNotes(leadId: String): Flow<List<LeadNote>> {
        return leadNoteDao.getByLeadId(leadId).map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(dispatcherProvider.io)
    }

    override suspend fun updateLeadStatus(leadId: String, status: String): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                // Update local DB
                val lead = leadDao.getById(leadId) ?: return@withContext Result.failure(
                    Exception("Lead not found")
                )
                
                // Find status ID from status value
                val statusEntity = leadStatusDao.getByName(status.replaceFirstChar { it.uppercase() })
                
                leadDao.update(lead.copy(
                    statusId = statusEntity?.id ?: lead.statusId,
                    syncStatus = 1, // PENDING
                    updatedAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ))
                
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    override suspend fun scheduleFollowUp(
        leadId: String,
        followUpDate: java.time.LocalDateTime,
        reminderNote: String?
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                val lead = leadDao.getById(leadId) ?: return@withContext Result.failure(
                    Exception("Lead not found")
                )
                
                val followUpMillis = followUpDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                leadDao.update(lead.copy(
                    nextFollowUpDate = followUpMillis,
                    reminderNote = reminderNote,
                    syncStatus = 1, // PENDING
                    updatedAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ))
                
                Result.success(Unit)
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }
    }

    private suspend fun getStatusMap(): Map<String, com.educonsult.crm.data.local.db.entity.LeadStatusEntity> {
        return try {
            leadStatusDao.getAll().first().associateBy { it.id }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
