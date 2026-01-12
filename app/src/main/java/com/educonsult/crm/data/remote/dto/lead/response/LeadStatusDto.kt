package com.educonsult.crm.data.remote.dto.lead.response

import kotlinx.serialization.Serializable

@Serializable
data class LeadStatusDto(
    val id: String,
    val name: String,
    val color: String,
    val description: String? = null,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)
