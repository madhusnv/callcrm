package com.educonsult.crm.domain.repository

import com.educonsult.crm.domain.model.Country
import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.domain.model.Institution

interface EducationRepository {
    suspend fun getCountries(
        includeInactive: Boolean = false,
        search: String? = null
    ): Result<List<Country>>

    suspend fun getInstitutions(
        countryId: String? = null,
        includeInactive: Boolean = false,
        search: String? = null
    ): Result<List<Institution>>

    suspend fun getCourses(
        countryId: String? = null,
        institutionId: String? = null,
        level: String? = null,
        includeInactive: Boolean = false,
        search: String? = null
    ): Result<List<Course>>

    suspend fun getCourseById(courseId: String): Result<Course>
}
