package com.educonsult.crm.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("expires_in")
    val expiresIn: Long,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("organization_id")
    val organizationId: String,
    @SerialName("branch_id")
    val branchId: String? = null
)
