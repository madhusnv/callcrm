package com.educonsult.crm.domain.repository

import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.model.LeadPriority
import com.educonsult.crm.domain.model.LeadStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LeadRepository {
    fun getLeads(filter: LeadFilter? = null): Flow<List<Lead>>
    suspend fun getLeadById(id: String): Lead?
    suspend fun getLeadByPhone(phone: String): Lead?
    suspend fun saveLead(lead: Lead): Result<Lead>
    suspend fun deleteLead(id: String): Result<Unit>
    suspend fun syncLeads(): Result<Int>
    fun getLeadStatuses(): Flow<List<LeadStatus>>
    fun getFollowUpsDue(): Flow<List<Lead>>
    suspend fun saveNote(leadId: String, note: LeadNote): Result<LeadNote>
    fun getNotes(leadId: String): Flow<List<LeadNote>>
    
    // Status and follow-up management
    suspend fun updateLeadStatus(leadId: String, status: String): Result<Unit>
    suspend fun scheduleFollowUp(
        leadId: String,
        followUpDate: java.time.LocalDateTime,
        reminderNote: String?
    ): Result<Unit>
}

data class LeadFilter(
    val statusId: String? = null,
    val priority: LeadPriority? = null,
    val assignedTo: String? = null,
    val source: String? = null,
    val searchQuery: String? = null,
    val followUpFrom: LocalDate? = null,
    val followUpTo: LocalDate? = null
)
