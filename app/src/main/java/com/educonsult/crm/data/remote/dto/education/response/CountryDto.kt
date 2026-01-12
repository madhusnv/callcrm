package com.educonsult.crm.data.remote.dto.education.response

import kotlinx.serialization.Serializable

@Serializable
data class CountryDto(
    val id: String,
    val name: String,
    val code: String,
    val currencyCode: String? = null,
    val phoneCode: String? = null,
    val displayOrder: Int = 0,
    val isActive: Boolean = true
)
