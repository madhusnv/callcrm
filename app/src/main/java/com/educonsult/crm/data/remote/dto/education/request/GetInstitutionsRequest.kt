package com.educonsult.crm.data.remote.dto.education.request

import kotlinx.serialization.Serializable

@Serializable
data class GetInstitutionsRequest(
    val includeInactive: Boolean? = null,
    val countryId: String? = null,
    val search: String? = null
)
