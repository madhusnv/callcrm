package com.educonsult.crm.data.remote.dto.lead.response

import kotlinx.serialization.Serializable

@Serializable
data class LeadResponse(
    val lead: LeadDto
)
