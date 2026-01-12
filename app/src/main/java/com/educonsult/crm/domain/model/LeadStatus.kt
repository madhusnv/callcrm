package com.educonsult.crm.domain.model

data class LeadStatus(
    val id: String,
    val name: String,
    val color: String,
    val description: String?,
    val sortOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean
)
