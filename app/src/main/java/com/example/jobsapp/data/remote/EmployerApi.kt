package com.example.jobsapp.data.remote

import com.example.jobsapp.data.dto.ApplicationStatusDto
import com.example.jobsapp.data.dto.EmployerApplicationResponse
import com.example.jobsapp.data.dto.ErrorResponse
import com.example.jobsapp.data.dto.UpdateApplicationStatusRequest
import com.example.jobsapp.data.dto.VacancyCreateRequest
import com.example.jobsapp.data.dto.VacancyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class EmployerApi(
    private val client: HttpClient
) {

    suspend fun getMyVacancies(): List<VacancyResponse> {
        val response = client.get("/api/employer/vacancies")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun createVacancy(
        request: VacancyCreateRequest
    ): VacancyResponse {
        val response = client.post("/api/employer/vacancies") {
            setBody(request)
        }

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun publishVacancy(
        vacancyId: String
    ) {
        val response = client.post("/api/employer/vacancies/$vacancyId/publish")

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.safeError())
        }
    }

    suspend fun resumeVacancy(
        vacancyId: String
    ) {
        val response = client.post("/api/employer/vacancies/$vacancyId/resume")

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.safeError())
        }
    }

    suspend fun archiveVacancy(
        vacancyId: String
    ) {
        val response = client.post("/api/employer/vacancies/$vacancyId/archive")

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.safeError())
        }
    }

    suspend fun getApplicationsByVacancy(
        vacancyId: String
    ): List<EmployerApplicationResponse> {
        val response = client.get("/api/employer/vacancies/$vacancyId/applications")

        if (response.status.isSuccess()) {
            return response.body()
        }

        throw IllegalStateException(response.safeError())
    }

    suspend fun updateApplicationStatus(
        applicationId: String,
        status: ApplicationStatusDto,
        message: String?
    ) {
        val response = client.patch("/api/employer/applications/$applicationId/status") {
            setBody(
                UpdateApplicationStatusRequest(
                    status = status,
                    message = message?.trim()?.takeIf { it.isNotBlank() }
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.safeError())
        }
    }

    private suspend fun HttpResponse.safeError(): String {
        return try {
            body<ErrorResponse>().message
        } catch (_: Exception) {
            "Ошибка сервера: ${status.value}"
        }
    }
}