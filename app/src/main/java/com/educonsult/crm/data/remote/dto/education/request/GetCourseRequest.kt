package com.educonsult.crm.data.remote.dto.education.request

import kotlinx.serialization.Serializable

@Serializable
data class GetCourseRequest(
    val courseId: String
)
