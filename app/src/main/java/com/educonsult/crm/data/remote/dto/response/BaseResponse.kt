package com.educonsult.crm.data.remote.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status: Boolean,
    val message: String? = null,
    val data: T? = null
)
