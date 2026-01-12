package com.educonsult.crm.data.remote.dto.education.request

import kotlinx.serialization.Serializable

@Serializable
data class GetCoursesRequest(
    val includeInactive: Boolean? = null,
    val countryId: String? = null,
    val institutionId: String? = null,
    val level: String? = null,
    val search: String? = null
)
