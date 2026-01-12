package com.educonsult.crm.data.repository

import com.educonsult.crm.data.local.db.dao.CallLogDao
import com.educonsult.crm.data.local.db.dao.CallRecordingDao
import com.educonsult.crm.data.local.db.dao.LeadDao
import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.data.local.db.entity.CallRecordingEntity
import com.educonsult.crm.data.remote.api.CallApi
import com.educonsult.crm.data.remote.dto.call.request.CallLogRequest
import com.educonsult.crm.data.remote.dto.call.request.ConfirmUploadRequest
import com.educonsult.crm.data.remote.dto.call.request.GetUploadUrlRequest
import com.educonsult.crm.data.remote.dto.call.request.SyncCallLogsRequest
import com.educonsult.crm.data.remote.dto.call.request.SyncNoteRequest
import com.educonsult.crm.data.remote.dto.call.response.UploadUrlResponse
import com.educonsult.crm.domain.repository.CallRepository
import com.educonsult.crm.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepositoryImpl @Inject constructor(
    private val callApi: CallApi,
    private val callLogDao: CallLogDao,
    private val callRecordingDao: CallRecordingDao,
    private val leadDao: LeadDao,
    private val dispatcherProvider: DispatcherProvider
) : CallRepository {

    private val isoFormatter = DateTimeFormatter.ISO_INSTANT

    // =========================================================================
    // Call Logs
    // =========================================================================

    override fun getAllCallLogs(): Flow<List<CallLogEntity>> {
        return callLogDao.getAll()
    }

    override fun getCallLogsByLead(leadId: String): Flow<List<CallLogEntity>> {
        return callLogDao.getByLeadId(leadId)
    }

    override suspend fun getCallLogById(id: String): CallLogEntity? {
        return withContext(dispatcherProvider.io) {
            callLogDao.getById(id)
        }
    }

    override suspend fun getCallLogByDeviceId(deviceCallId: String): CallLogEntity? {
        return withContext(dispatcherProvider.io) {
            callLogDao.getByDeviceCallId(deviceCallId)
        }
    }

    override suspend fun insertCallLog(callLog: CallLogEntity) {
        withContext(dispatcherProvider.io) {
            callLogDao.insert(callLog)
            
            // Try to match to existing lead by phone
            val normalizedPhone = normalizePhone(callLog.phoneNumber)
            val lead = leadDao.getByPhone(normalizedPhone)
                ?: leadDao.getByPhone(callLog.phoneNumber)
            
            if (lead != null && callLog.leadId == null) {
                callLogDao.updateLeadId(callLog.id, lead.id, System.currentTimeMillis())
                leadDao.incrementCallCount(lead.id, callLog.callAt)
            }
        }
    }

    override suspend fun updateCallLogNotes(id: String, notes: String) {
        withContext(dispatcherProvider.io) {
            callLogDao.updateNotes(id, notes, System.currentTimeMillis())
        }
    }

    override suspend fun matchCallToLead(callLogId: String, leadId: String) {
        withContext(dispatcherProvider.io) {
            callLogDao.updateLeadId(callLogId, leadId, System.currentTimeMillis())
        }
    }

    // =========================================================================
    // Sync
    // =========================================================================

    override suspend fun syncCallLogs(): Result<Int> {
        return withContext(dispatcherProvider.io) {
            try {
                val pendingLogs = callLogDao.getPendingSync()
                if (pendingLogs.isEmpty()) {
                    return@withContext Result.success(0)
                }

                val requests = pendingLogs.map { log ->
                    CallLogRequest(
                        phoneNumber = log.phoneNumber,
                        callType = log.callType,
                        duration = log.duration,
                        callAt = Instant.ofEpochMilli(log.callAt).toString(),
                        deviceCallId = log.deviceCallId,
                        simSlot = log.simSlot,
                        contactName = log.contactName,
                        notes = log.notes
                    )
                }

                val response = callApi.syncCallLogs(SyncCallLogsRequest(requests))
                if (response.isSuccessful && response.body()?.status == true) {
                    val result = response.body()?.data
                    val now = System.currentTimeMillis()
                    
                    // Mark all as synced
                    pendingLogs.forEach { log ->
                        callLogDao.updateSyncStatus(log.id, CallLogEntity.SYNC_SYNCED, now)
                    }
                    
                    Result.success(result?.created ?: 0 + (result?.updated ?: 0))
                } else {
                    Result.failure(Exception("Sync failed: ${response.body()?.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getPendingSyncCount(): Int {
        return withContext(dispatcherProvider.io) {
            callLogDao.getPendingSyncCount()
        }
    }

    // =========================================================================
    // Recordings
    // =========================================================================

    override suspend fun getRecordingByCallLogId(callLogId: String): CallRecordingEntity? {
        return withContext(dispatcherProvider.io) {
            callRecordingDao.getByCallLogId(callLogId)
        }
    }

    override suspend fun createRecordingEntry(callLogId: String): String {
        return withContext(dispatcherProvider.io) {
            val id = UUID.randomUUID().toString()
            val recording = CallRecordingEntity(
                id = id,
                callLogId = callLogId,
                status = CallRecordingEntity.STATUS_PENDING
            )
            callRecordingDao.insert(recording)
            id
        }
    }

    override suspend fun updateRecordingLocalFile(recordingId: String, filePath: String, fileSize: Long) {
        withContext(dispatcherProvider.io) {
            callRecordingDao.updateLocalFile(recordingId, filePath, fileSize, System.currentTimeMillis())
        }
    }

    override suspend fun getUploadUrl(callLogId: String): Result<UploadUrlResponse> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = callApi.getUploadUrl(GetUploadUrlRequest(callLogId = callLogId))
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        Result.success(data)
                    } else {
                        Result.failure(Exception("No upload URL returned"))
                    }
                } else {
                    Result.failure(Exception("Failed to get upload URL: ${response.body()?.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun confirmUpload(recordingId: String, fileSize: Long, duration: Int): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                val request = ConfirmUploadRequest(
                    recordingId = recordingId,
                    compressedFileSize = fileSize,
                    duration = duration,
                    format = "mp3",
                    bitrate = 32
                )
                val response = callApi.confirmUpload(request)
                if (response.isSuccessful && response.body()?.status == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Confirm failed: ${response.body()?.message}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun markRecordingFailed(recordingId: String, error: String) {
        withContext(dispatcherProvider.io) {
            callRecordingDao.markFailed(recordingId, error, System.currentTimeMillis())
        }
    }

    override suspend fun getPendingRecordings(): List<CallRecordingEntity> {
        return withContext(dispatcherProvider.io) {
            callRecordingDao.getPendingUploads()
        }
    }

    // =========================================================================
    // Streaming
    // =========================================================================

    override suspend fun getStreamUrl(recordingId: String): Result<String> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = callApi.getStreamUrl(recordingId)
                if (response.isSuccessful && response.body()?.status == true) {
                    val url = response.body()?.data?.streamUrl
                    if (url != null) {
                        Result.success(url)
                    } else {
                        Result.failure(Exception("No stream URL returned"))
                    }
                } else {
                    Result.failure(Exception("Failed to get stream URL"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun normalizePhone(phone: String): String {
        return phone
            .replace(Regex("[\\s\\-\\(\\)]"), "")
            .replace(Regex("^\\+91"), "")
            .replace(Regex("^91(?=\\d{10}$)"), "")
    }
}
