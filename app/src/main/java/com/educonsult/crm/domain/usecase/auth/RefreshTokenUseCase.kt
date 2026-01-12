package com.educonsult.crm.domain.usecase.auth

import com.educonsult.crm.domain.repository.AuthRepository
import javax.inject.Inject

class RefreshTokenUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<String> {
        return authRepository.refreshToken()
    }
}
