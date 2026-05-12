package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.ApplyRequest
import com.example.jobsapp.data.dto.ApplicationResponse
import com.example.jobsapp.data.dto.ErrorResponse
import com.example.jobsapp.data.dto.FavoriteResponse
import com.example.jobsapp.data.dto.VacancyListResponse
import com.example.jobsapp.data.dto.VacancyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class VacancyApi(
    private val client: HttpClient
) {

    suspend fun getVacancies(
        page: Int = 1,
        size: Int = 20,
        query: String? = null,
        workFormat: String? = null
    ): VacancyListResponse {
        val response = client.get("/api/vacancies") {
            url {
                parameters.append("page", page.toString())
                parameters.append("size", size.toString())

                if (!query.isNullOrBlank()) {
                    parameters.append("query", query)
                }

                if (!workFormat.isNullOrBlank()) {
                    parameters.append("workFormat", workFormat)
                }

                parameters.append("sort", "createdAt_desc")
            }
        }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun getVacancyById(
        id: String
    ): VacancyResponse {
        val response = client.get("/api/vacancies/$id")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun applyToVacancy(
        vacancyId: String,
        coverLetter: String
    ): ApplicationResponse {
        val response = client.post("/api/vacancies/$vacancyId/applications") {
            setBody(
                ApplyRequest(
                    coverLetter = coverLetter
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun addFavorite(
        vacancyId: String
    ): FavoriteResponse {
        val response = client.post("/api/vacancies/$vacancyId/favorite")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun removeFavorite(
        vacancyId: String
    ) {
        val response = client.delete("/api/vacancies/$vacancyId/favorite")

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.safeError())
        }
    }

    suspend fun getFavorites(): List<VacancyResponse> {
        val response = client.get("/api/me/favorites")

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