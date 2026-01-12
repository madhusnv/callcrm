package com.educonsult.crm.data.remote.dto.lead.response

import kotlinx.serialization.Serializable

@Serializable
data class LeadListResponse(
    val leads: List<LeadDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val hasMore: Boolean
)
