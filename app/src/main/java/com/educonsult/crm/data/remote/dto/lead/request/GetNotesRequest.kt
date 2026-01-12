package com.educonsult.crm.data.remote.dto.lead.request

import kotlinx.serialization.Serializable

@Serializable
data class GetNotesRequest(
    val leadId: String,
    val page: Int = 1,
    val limit: Int = 50
)
