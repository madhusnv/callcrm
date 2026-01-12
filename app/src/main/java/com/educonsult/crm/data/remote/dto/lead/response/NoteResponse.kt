package com.educonsult.crm.data.remote.dto.lead.response

import kotlinx.serialization.Serializable

@Serializable
data class NoteResponse(
    val note: NoteDto
)
