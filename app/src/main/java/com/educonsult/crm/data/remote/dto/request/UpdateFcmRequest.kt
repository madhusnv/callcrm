package com.educonsult.crm.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFcmRequest(
    val token: String,
    val deviceInfo: Map<String, String>? = null
)
