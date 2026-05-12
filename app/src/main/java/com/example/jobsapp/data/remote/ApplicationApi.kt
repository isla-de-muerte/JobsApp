package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.ApplicationResponse
import com.example.jobsapp.data.dto.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class ApplicationApi(
    private val client: HttpClient
) {

    suspend fun getMyApplications(): List<ApplicationResponse> {
        val response = client.get("/api/me/applications")

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