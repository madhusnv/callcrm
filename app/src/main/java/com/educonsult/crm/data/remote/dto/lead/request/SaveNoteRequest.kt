package com.educonsult.crm.data.remote.dto.lead.request

import kotlinx.serialization.Serializable

@Serializable
data class SaveNoteRequest(
    val id: String? = null,
    val leadId: String,
    val content: String,
    val noteType: String = "general"
)
