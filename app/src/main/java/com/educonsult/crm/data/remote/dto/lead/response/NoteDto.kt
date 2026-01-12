package com.educonsult.crm.data.remote.dto.lead.response

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val leadId: String,
    val content: String,
    val noteType: String = "general",
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String
)
