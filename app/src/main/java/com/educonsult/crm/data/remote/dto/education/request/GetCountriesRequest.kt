package com.educonsult.crm.data.remote.dto.education.request

import kotlinx.serialization.Serializable

@Serializable
data class GetCountriesRequest(
    val includeInactive: Boolean? = null,
    val search: String? = null
)
