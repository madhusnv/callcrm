package com.educonsult.crm.data.remote.dto.lead.request

import kotlinx.serialization.Serializable

@Serializable
data class GetLeadByIdRequest(
    val leadId: String
)
