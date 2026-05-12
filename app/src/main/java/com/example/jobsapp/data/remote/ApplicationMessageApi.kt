package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.ApplicationMessageResponse
import com.example.jobsapp.data.dto.ApplicationUnreadCountResponse
import com.example.jobsapp.data.dto.ErrorResponse
import com.example.jobsapp.data.dto.SendApplicationMessageRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class ApplicationMessageApi(
    private val client: HttpClient
) {

    suspend fun getMessages(
        applicationId: String
    ): List<ApplicationMessageResponse> {
        val response = client.get("/api/applications/$applicationId/messages")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun sendMessage(
        applicationId: String,
        message: String
    ): ApplicationMessageResponse {
        val response = client.post("/api/applications/$applicationId/messages") {
            setBody(
                SendApplicationMessageRequest(
                    message = message
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun markAsRead(
        applicationId: String
    ) {
        val response = client.post("/api/applications/$applicationId/read")

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.safeError())
        }
    }

    suspend fun getUnreadCounts(): List<ApplicationUnreadCountResponse> {
        val response = client.get("/api/applications/unread-counts")

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