package com.educonsult.crm.domain.model

data class Institution(
    val id: String,
    val countryId: String?,
    val name: String,
    val city: String?,
    val institutionType: String?,
    val website: String?,
    val logoUrl: String?,
    val displayOrder: Int,
    val isActive: Boolean
)
