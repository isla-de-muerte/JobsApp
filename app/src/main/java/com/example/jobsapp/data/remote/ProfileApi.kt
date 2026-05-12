package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.ApplicantProfileRequest
import com.example.jobsapp.data.dto.ApplicantProfileResponse
import com.example.jobsapp.data.dto.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class ProfileApi(
    private val client: HttpClient
) {

    suspend fun getProfile(): ApplicantProfileResponse {
        val response = client.get("/api/me/profile")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun saveProfile(
        request: ApplicantProfileRequest
    ): ApplicantProfileResponse {
        val response = client.put("/api/me/profile") {
            setBody(request)
        }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    private suspend fun HttpResponse.safeError(): String {
        return try {
            body<ErrorResponse>().message
        } catch (_: Exception) {
            "Ошибка сервера: ${status.value}"
        }
    }
}