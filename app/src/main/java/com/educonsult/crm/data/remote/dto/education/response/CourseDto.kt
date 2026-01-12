package com.educonsult.crm.data.remote.dto.education.response

import kotlinx.serialization.Serializable

@Serializable
data class CourseDto(
    val id: String,
    val countryId: String? = null,
    val institutionId: String? = null,
    val name: String,
    val level: String? = null,
    val durationMonths: Int? = null,
    val intakeMonths: List<String> = emptyList(),
    val tuitionFee: Double? = null,
    val currencyCode: String? = null,
    val description: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
