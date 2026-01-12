package com.educonsult.crm.data.remote.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)
