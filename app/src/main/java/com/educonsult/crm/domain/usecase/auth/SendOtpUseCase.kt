package com.educonsult.crm.domain.usecase.auth

import com.educonsult.crm.domain.repository.AuthRepository
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<Unit> {
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone number cannot be empty"))
        }
        if (!isValidPhone(phone)) {
            return Result.failure(IllegalArgumentException("Invalid phone number format"))
        }

        return authRepository.sendOtp(phone.trim())
    }

    private fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[\\s\\-()]"), "")
        return cleaned.length in 10..15 && cleaned.all { it.isDigit() || it == '+' }
    }
}
