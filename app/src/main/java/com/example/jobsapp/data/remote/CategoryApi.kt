package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.CategoryResponse
import com.example.jobsapp.data.dto.ErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class CategoryApi(
    private val client: HttpClient
) {

    suspend fun getCategories(): List<CategoryResponse> {
        val response = client.get("/api/categories")

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