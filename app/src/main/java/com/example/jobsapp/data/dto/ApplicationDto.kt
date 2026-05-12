package com.example.jobsapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationResponse(
    val id: String,
    val vacancyId: String,
    val applicantId: String,
    val profileId: String,
    val coverLetter: String?,
    val status: String,
    val createdAt: String,
    val updatedAt: String? = null
)