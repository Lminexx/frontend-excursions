package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class CreatingExcursion(
    val title: String,
    val description: String
)