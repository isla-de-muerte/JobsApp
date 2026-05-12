package com.example.jobsapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
enum class WorkFormatDto {
    REMOTE,
    PART_TIME,
    HYBRID,
    OFFICE
}

@Serializable
enum class VacancyStatusDto {
    DRAFT,
    PUBLISHED,
    PAUSED_BY_OVERLOAD,
    ARCHIVED
}

@Serializable
data class VacancyResponse(
    val id: String,
    val employerId: String,
    val categoryId: String,
    val title: String,
    val description: String,
    val requirements: String? = null,
    val salaryFrom: Int? = null,
    val salaryTo: Int? = null,
    val workFormat: WorkFormatDto,
    val status: VacancyStatusDto,
    val viewsCount: Int,
    val applicationsCount: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class VacancyListResponse(
    val items: List<VacancyResponse>,
    val page: Int,
    val size: Int,
    val total: Long
)

@Serializable
data class VacancyCreateRequest(
    val categoryId: String,
    val title: String,
    val description: String,
    val requirements: String? = null,
    val salaryFrom: Int? = null,
    val salaryTo: Int? = null,
    val workFormat: WorkFormatDto
)

@Serializable
data class ApplyRequest(
    val coverLetter: String? = null
)

@Serializable
data class FavoriteResponse(
    val id: String,
    val vacancyId: String,
    val createdAt: String
)