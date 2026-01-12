package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.dashboard.response.DashboardStatsDto
import com.educonsult.crm.data.remote.dto.response.BaseResponse
import retrofit2.Response
import retrofit2.http.POST

interface DashboardApi {

    @POST("dashboard/stats")
    suspend fun getStats(): Response<BaseResponse<DashboardStatsDto>>
}
