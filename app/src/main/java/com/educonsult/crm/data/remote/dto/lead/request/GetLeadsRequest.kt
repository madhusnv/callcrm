package com.educonsult.crm.data.remote.dto.lead.request

import kotlinx.serialization.Serializable

@Serializable
data class GetLeadsRequest(
    val statusId: String? = null,
    val priority: String? = null,
    val assignedTo: String? = null,
    val source: String? = null,
    val searchQuery: String? = null,
    val followUpFrom: String? = null,
    val followUpTo: String? = null,
    val page: Int = 1,
    val limit: Int = 50,
    val lastSyncTime: Long? = null
)
