package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class CreatingExcursion(
    val title: String,
    val description: String,
    val cityName: String,
    val tags: List<String>,
    val topic: String
)