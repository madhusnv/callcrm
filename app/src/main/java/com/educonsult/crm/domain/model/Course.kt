package com.educonsult.crm.domain.model

data class Course(
    val id: String,
    val countryId: String?,
    val institutionId: String?,
    val name: String,
    val level: String?,
    val durationMonths: Int?,
    val intakeMonths: List<String>,
    val tuitionFee: Double?,
    val currencyCode: String?,
    val description: String?,
    val displayOrder: Int,
    val isActive: Boolean
)
