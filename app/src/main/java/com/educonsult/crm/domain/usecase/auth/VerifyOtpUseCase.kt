package com.educonsult.crm.domain.usecase.auth

import com.educonsult.crm.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, otp: String): Result<Boolean> {
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        }
        if (otp.isBlank()) {
            return Result.failure(IllegalArgumentException("OTP cannot be empty"))
        }
        if (otp.length !in 4..6) {
            return Result.failure(IllegalArgumentException("Invalid OTP format"))
        }

        return authRepository.verifyOtp(phone.trim(), otp.trim())
    }
}
