package com.educonsult.crm.data.remote.interceptor

import com.educonsult.crm.data.local.ApiKeyManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyInterceptor @Inject constructor(
    private val apiKeyManager: ApiKeyManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestPath = originalRequest.url.encodedPath

        if (isExcludedEndpoint(requestPath)) {
            return chain.proceed(originalRequest)
        }

        val apiKey = apiKeyManager.getApiKey()
            ?: throw ApiKeyMissingException("API key is required but not configured")

        val authenticatedRequest = originalRequest.newBuilder()
            .header(HEADER_API_KEY, apiKey)
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private fun isExcludedEndpoint(path: String): Boolean {
        return EXCLUDED_ENDPOINTS.any { path.contains(it, ignoreCase = true) }
    }

    companion object {
        private const val HEADER_API_KEY = "X-API-Key"

        private val EXCLUDED_ENDPOINTS = listOf(
            "/api/v1/settings/validate-api-key",
            "/api/v1/settings/app"
        )
    }
}

class ApiKeyMissingException(message: String) : RuntimeException(message)
