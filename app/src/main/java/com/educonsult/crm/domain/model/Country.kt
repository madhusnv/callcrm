package com.educonsult.crm.domain.model

data class Country(
    val id: String,
    val name: String,
    val code: String,
    val currencyCode: String?,
    val phoneCode: String?,
    val displayOrder: Int,
    val isActive: Boolean
)
