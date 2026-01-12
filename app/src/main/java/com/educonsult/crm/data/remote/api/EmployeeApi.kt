package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.request.UpdateFcmRequest
import com.educonsult.crm.data.remote.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EmployeeApi {

    @POST("employee/updateFCM")
    suspend fun updateFcmToken(
        @Body request: UpdateFcmRequest
    ): Response<BaseResponse<Map<String, String>>>
}
