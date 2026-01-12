package com.educonsult.crm.domain.usecase.employee

import com.educonsult.crm.domain.repository.EmployeeRepository
import javax.inject.Inject

class UpdateFcmTokenUseCase @Inject constructor(
    private val employeeRepository: EmployeeRepository
) {
    suspend operator fun invoke(
        token: String,
        deviceInfo: Map<String, String>? = null
    ): Result<Unit> {
        return employeeRepository.updateFcmToken(token, deviceInfo)
    }
}
