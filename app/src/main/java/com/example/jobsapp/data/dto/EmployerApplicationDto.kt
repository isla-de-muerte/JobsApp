package com.example.jobsapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class EmployerApplicationResponse(
    val id: String,
    val vacancyId: String,
    val applicantId: String,
    val coverLetter: String?,
    val status: String,
    val createdAt: String,
    val resume: ApplicantProfileResponse
)

@Serializable
enum class ApplicationStatusDto {
    NEW,
    VIEWED,
    INTERVIEW,
    REJECTED,
    ACCEPTED
}

@Serializable
data class UpdateApplicationStatusRequest(
    val status: ApplicationStatusDto,
    val message: String? = null
)