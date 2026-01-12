package com.educonsult.crm.domain.repository

interface EmployeeRepository {
    suspend fun updateFcmToken(
        token: String,
        deviceInfo: Map<String, String>? = null
    ): Result<Unit>
}
