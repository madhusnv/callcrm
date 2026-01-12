package com.educonsult.crm.domain.usecase.auth

import com.educonsult.crm.domain.model.User
import com.educonsult.crm.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        if (firstName.isBlank()) {
            return Result.failure(IllegalArgumentException("First name cannot be empty"))
        }
        if (lastName.isBlank()) {
            return Result.failure(IllegalArgumentException("Last name cannot be empty"))
        }
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email cannot be empty"))
        }
        if (!isValidEmail(email)) {
            return Result.failure(IllegalArgumentException("Invalid email format"))
        }
        if (phone.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone cannot be empty"))
        }
        if (!isValidPhone(phone)) {
            return Result.failure(IllegalArgumentException("Invalid phone number format"))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters"))
        }

        return authRepository.register(
            firstName = firstName.trim(),
            lastName = lastName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            password = password
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[\\s\\-()]"), "")
        return cleaned.length in 10..15 && cleaned.all { it.isDigit() || it == '+' }
    }
}
