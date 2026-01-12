package com.educonsult.crm.data.repository

import com.educonsult.crm.data.mapper.toDomain
import com.educonsult.crm.data.remote.api.EducationApi
import com.educonsult.crm.data.remote.dto.education.request.GetCountriesRequest
import com.educonsult.crm.data.remote.dto.education.request.GetCourseRequest
import com.educonsult.crm.data.remote.dto.education.request.GetCoursesRequest
import com.educonsult.crm.data.remote.dto.education.request.GetInstitutionsRequest
import com.educonsult.crm.domain.model.Country
import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.domain.model.Institution
import com.educonsult.crm.domain.repository.EducationRepository
import com.educonsult.crm.util.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EducationRepositoryImpl @Inject constructor(
    private val educationApi: EducationApi,
    private val dispatcherProvider: DispatcherProvider
) : EducationRepository {

    override suspend fun getCountries(
        includeInactive: Boolean,
        search: String?
    ): Result<List<Country>> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = educationApi.getCountries(
                    GetCountriesRequest(
                        includeInactive = includeInactive,
                        search = search
                    )
                )
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data.orEmpty()
                    Result.success(data.map { it.toDomain() })
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getInstitutions(
        countryId: String?,
        includeInactive: Boolean,
        search: String?
    ): Result<List<Institution>> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = educationApi.getInstitutions(
                    GetInstitutionsRequest(
                        includeInactive = includeInactive,
                        countryId = countryId,
                        search = search
                    )
                )
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data.orEmpty()
                    Result.success(data.map { it.toDomain() })
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getCourses(
        countryId: String?,
        institutionId: String?,
        level: String?,
        includeInactive: Boolean,
        search: String?
    ): Result<List<Course>> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = educationApi.getCourses(
                    GetCoursesRequest(
                        includeInactive = includeInactive,
                        countryId = countryId,
                        institutionId = institutionId,
                        level = level,
                        search = search
                    )
                )
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data.orEmpty()
                    Result.success(data.map { it.toDomain() })
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getCourseById(courseId: String): Result<Course> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = educationApi.getCourse(GetCourseRequest(courseId))
                if (response.isSuccessful && response.body()?.status == true) {
                    val course = response.body()?.data
                    if (course != null) {
                        Result.success(course.toDomain())
                    } else {
                        Result.failure(Exception("Course not found"))
                    }
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
