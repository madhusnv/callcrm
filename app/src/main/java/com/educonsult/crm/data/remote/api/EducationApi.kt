package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.education.request.GetCountriesRequest
import com.educonsult.crm.data.remote.dto.education.request.GetCourseRequest
import com.educonsult.crm.data.remote.dto.education.request.GetCoursesRequest
import com.educonsult.crm.data.remote.dto.education.request.GetInstitutionsRequest
import com.educonsult.crm.data.remote.dto.education.response.CountryDto
import com.educonsult.crm.data.remote.dto.education.response.CourseDto
import com.educonsult.crm.data.remote.dto.education.response.InstitutionDto
import com.educonsult.crm.data.remote.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EducationApi {

    @POST("education/countries")
    suspend fun getCountries(
        @Body request: GetCountriesRequest
    ): Response<BaseResponse<List<CountryDto>>>

    @POST("education/institutions")
    suspend fun getInstitutions(
        @Body request: GetInstitutionsRequest
    ): Response<BaseResponse<List<InstitutionDto>>>

    @POST("education/courses")
    suspend fun getCourses(
        @Body request: GetCoursesRequest
    ): Response<BaseResponse<List<CourseDto>>>

    @POST("education/course/get")
    suspend fun getCourse(
        @Body request: GetCourseRequest
    ): Response<BaseResponse<CourseDto>>
}
