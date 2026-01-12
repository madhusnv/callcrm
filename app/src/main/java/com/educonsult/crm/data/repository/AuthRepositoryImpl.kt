package com.educonsult.crm.data.repository

import com.educonsult.crm.data.local.TokenManager
import com.educonsult.crm.data.local.datastore.UserPreferences
import com.educonsult.crm.data.local.db.dao.UserDao
import com.educonsult.crm.data.mapper.toDomain
import com.educonsult.crm.data.mapper.toUserEntity
import com.educonsult.crm.data.remote.api.AuthApi
import com.educonsult.crm.data.remote.dto.request.LoginRequest
import com.educonsult.crm.data.remote.dto.request.RefreshTokenRequest
import com.educonsult.crm.data.remote.dto.request.RegisterRequest
import com.educonsult.crm.data.remote.dto.request.SendOtpRequest
import com.educonsult.crm.data.remote.dto.request.VerifyOtpRequest
import com.educonsult.crm.domain.model.User
import com.educonsult.crm.domain.repository.AuthRepository
import com.educonsult.crm.util.DispatcherProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager,
    private val userPreferences: UserPreferences,
    private val userDao: UserDao,
    private val dispatcherProvider: DispatcherProvider
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = authApi.login(LoginRequest(username = email, password = password))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true && body.data != null) {
                        val loginResponse = body.data
                        
                        tokenManager.saveAccessToken(loginResponse.accessToken)
                        tokenManager.saveRefreshToken(loginResponse.refreshToken)
                        
                        val userEntity = loginResponse.toUserEntity()
                        userDao.insertUser(userEntity)
                        
                        userPreferences.setLoggedIn(true)
                        userPreferences.setUserId(loginResponse.userId)
                        userPreferences.setOrganizationId(loginResponse.organizationId)
                        userPreferences.setBranchId(loginResponse.branchId)
                        
                        Result.success(userEntity.toDomain())
                    } else {
                        Result.failure(Exception(body?.message ?: "Login failed"))
                    }
                } else {
                    Result.failure(Exception("Login failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun logout(): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                authApi.logout()
                
                tokenManager.clearTokens()
                userDao.deleteAllUsers()
                userPreferences.clear()
                
                Result.success(Unit)
            } catch (e: Exception) {
                tokenManager.clearTokens()
                userDao.deleteAllUsers()
                userPreferences.clear()
                
                Result.success(Unit)
            }
        }
    }

    override suspend fun refreshToken(): Result<String> {
        return withContext(dispatcherProvider.io) {
            try {
                val currentRefreshToken = tokenManager.getRefreshToken()
                    ?: return@withContext Result.failure(Exception("No refresh token available"))
                
                val response = authApi.refreshToken(RefreshTokenRequest(refreshToken = currentRefreshToken))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true && body.data != null) {
                        val tokenResponse = body.data
                        
                        tokenManager.saveAccessToken(tokenResponse.accessToken)
                        tokenManager.saveRefreshToken(tokenResponse.refreshToken)
                        
                        Result.success(tokenResponse.accessToken)
                    } else {
                        Result.failure(Exception(body?.message ?: "Token refresh failed"))
                    }
                } else {
                    Result.failure(Exception("Token refresh failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        return withContext(dispatcherProvider.io) {
            try {
                val organizationId = userPreferences.organizationId.first()
                    ?: return@withContext Result.failure(Exception("Organization ID not found"))
                
                val response = authApi.register(
                    RegisterRequest(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phone = phone,
                        password = password,
                        organizationId = organizationId
                    )
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true && body.data != null) {
                        val loginResponse = body.data
                        
                        tokenManager.saveAccessToken(loginResponse.accessToken)
                        tokenManager.saveRefreshToken(loginResponse.refreshToken)
                        
                        val userEntity = loginResponse.toUserEntity()
                        userDao.insertUser(userEntity)
                        
                        userPreferences.setLoggedIn(true)
                        userPreferences.setUserId(loginResponse.userId)
                        userPreferences.setOrganizationId(loginResponse.organizationId)
                        userPreferences.setBranchId(loginResponse.branchId)
                        
                        Result.success(userEntity.toDomain())
                    } else {
                        Result.failure(Exception(body?.message ?: "Registration failed"))
                    }
                } else {
                    Result.failure(Exception("Registration failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun sendOtp(phone: String): Result<Unit> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = authApi.sendOtp(SendOtpRequest(phone = phone))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true) {
                        Result.success(Unit)
                    } else {
                        Result.failure(Exception(body?.message ?: "Failed to send OTP"))
                    }
                } else {
                    Result.failure(Exception("Failed to send OTP: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun verifyOtp(phone: String, otp: String): Result<Boolean> {
        return withContext(dispatcherProvider.io) {
            try {
                val response = authApi.verifyOtp(VerifyOtpRequest(phone = phone, otp = otp))
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true) {
                        Result.success(true)
                    } else {
                        Result.failure(Exception(body?.message ?: "OTP verification failed"))
                    }
                } else {
                    Result.failure(Exception("OTP verification failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun isLoggedIn(): Flow<Boolean> {
        return userPreferences.isLoggedIn
    }

    override suspend fun getCurrentUser(): Result<User> {
        return withContext(dispatcherProvider.io) {
            try {
                val userId = userPreferences.userId.first()
                    ?: return@withContext Result.failure(Exception("No user logged in"))
                
                val userEntity = userDao.getUserById(userId)
                    ?: return@withContext Result.failure(Exception("User not found"))
                
                Result.success(userEntity.toDomain())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
