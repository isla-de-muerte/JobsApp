package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.AuthResponse
import com.example.jobsapp.data.dto.ErrorResponse
import com.example.jobsapp.data.dto.LoginRequest
import com.example.jobsapp.data.dto.LogoutRequest
import com.example.jobsapp.data.dto.RefreshRequest
import com.example.jobsapp.data.dto.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class AuthApi(
    private val client: HttpClient
) {

    suspend fun login(
        request: LoginRequest
    ): AuthResponse {
        val response = client.post("/api/auth/login") {
            setBody(request)
        }

        return response.parseAuthResponse()
    }

    suspend fun register(
        request: RegisterRequest
    ): AuthResponse {
        val response = client.post("/api/auth/register") {
            setBody(request)
        }

        return response.parseAuthResponse()
    }

    suspend fun refresh(
        refreshToken: String
    ): AuthResponse {
        val response = client.post("/api/auth/refresh") {
            setBody(
                RefreshRequest(
                    refreshToken = refreshToken
                )
            )
        }

        return response.parseAuthResponse()
    }

    suspend fun logout(
        refreshToken: String
    ) {
        val response = client.post("/api/auth/logout") {
            setBody(
                LogoutRequest(
                    refreshToken = refreshToken
                )
            )
        }

        if (!response.status.isSuccess()) {
            val error = response.safeError()
            throw IllegalStateException(error)
        }
    }

    private suspend fun HttpResponse.parseAuthResponse(): AuthResponse {
        if (status.isSuccess()) {
            return body<AuthResponse>()
        }

        val errorMessage = safeError()
        throw IllegalStateException(errorMessage)
    }

    private suspend fun HttpResponse.safeError(): String {
        return try {
            body<ErrorResponse>().message
        } catch (_: Exception) {
            "Ошибка сервера: ${status.value}"
        }
    }
}