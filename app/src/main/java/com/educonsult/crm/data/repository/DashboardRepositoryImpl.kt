package com.educonsult.crm.data.repository

import com.educonsult.crm.data.mapper.toDomain
import com.educonsult.crm.data.remote.api.DashboardApi
import com.educonsult.crm.domain.model.DashboardStats
import com.educonsult.crm.domain.repository.DashboardRepository
import com.educonsult.crm.util.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val dispatcherProvider: DispatcherProvider
) : DashboardRepository {

    override suspend fun getStats(): Result<DashboardStats> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = dashboardApi.getStats()
                if (response.isSuccessful && response.body()?.status == true) {
                    val data = response.body()?.data
                    if (data != null) {
                        Result.success(data.toDomain())
                    } else {
                        Result.failure(Exception("No dashboard data"))
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
