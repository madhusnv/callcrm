package com.educonsult.crm.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    @SerialName("organization_id")
    val organizationId: String
)
