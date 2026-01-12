package com.educonsult.crm.data.remote.dto.education.response

import kotlinx.serialization.Serializable

@Serializable
data class InstitutionDto(
    val id: String,
    val countryId: String? = null,
    val name: String,
    val city: String? = null,
    val institutionType: String? = null,
    val website: String? = null,
    val logoUrl: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
