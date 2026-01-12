package com.educonsult.crm.data.remote.api

import com.educonsult.crm.data.remote.dto.request.LoginRequest
import com.educonsult.crm.data.remote.dto.request.RefreshTokenRequest
import com.educonsult.crm.data.remote.dto.request.RegisterRequest
import com.educonsult.crm.data.remote.dto.request.SendOtpRequest
import com.educonsult.crm.data.remote.dto.request.VerifyOtpRequest
import com.educonsult.crm.data.remote.dto.response.BaseResponse
import com.educonsult.crm.data.remote.dto.response.LoginResponse
import com.educonsult.crm.data.remote.dto.response.RefreshTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("oauth/token")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<BaseResponse<RefreshTokenResponse>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    @POST("auth/register/sendOTP")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<BaseResponse<Unit>>

    @POST("auth/register/verifyOTP")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<BaseResponse<Unit>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<BaseResponse<LoginResponse>>
}
