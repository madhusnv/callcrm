package com.educonsult.crm.data.repository

import com.educonsult.crm.data.remote.api.EmployeeApi
import com.educonsult.crm.data.remote.dto.request.UpdateFcmRequest
import com.educonsult.crm.domain.repository.EmployeeRepository
import com.educonsult.crm.util.DispatcherProvider
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepositoryImpl @Inject constructor(
    private val employeeApi: EmployeeApi,
    private val dispatcherProvider: DispatcherProvider
) : EmployeeRepository {

    override suspend fun updateFcmToken(
        token: String,
        deviceInfo: Map<String, String>?
    ): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = employeeApi.updateFcmToken(
                    UpdateFcmRequest(
                        token = token,
                        deviceInfo = deviceInfo
                    )
                )
                if (response.isSuccessful && response.body()?.status == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
