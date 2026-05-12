package com.example.jobsapp.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String
)