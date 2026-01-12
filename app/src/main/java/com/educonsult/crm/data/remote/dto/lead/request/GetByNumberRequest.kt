package com.educonsult.crm.data.remote.dto.lead.request

import kotlinx.serialization.Serializable

@Serializable
data class GetByNumberRequest(
    val phone: String,
    val countryCode: Int = 91
)
