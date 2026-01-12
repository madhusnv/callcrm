package com.educonsult.crm.domain.model

import java.time.LocalDateTime

data class LeadNote(
    val id: String,
    val leadId: String,
    val content: String,
    val noteType: NoteType,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

enum class NoteType {
    GENERAL,
    CALL,
    MEETING,
    EMAIL,
    FOLLOW_UP,
    STATUS_CHANGE
}
