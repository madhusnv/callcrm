package com.educonsult.crm.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val phone: String
)
