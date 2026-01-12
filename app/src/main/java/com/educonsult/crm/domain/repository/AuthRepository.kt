package com.educonsult.crm.domain.repository

import com.educonsult.crm.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): Result<User>
    suspend fun refreshToken(): Result<String>
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, otp: String): Result<Boolean>
    fun isLoggedIn(): Flow<Boolean>
    suspend fun getCurrentUser(): Result<User>
}
