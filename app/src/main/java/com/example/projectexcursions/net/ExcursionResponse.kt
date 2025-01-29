package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse(
    val id: Long,
    val title: String,
    val userId: Long,
    val description: String,
    val username: String,
    val favorite: Boolean = false
)