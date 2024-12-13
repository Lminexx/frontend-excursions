package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse (
    val id: Long,
    val title: String,
    val description: String,
    val username: String
)