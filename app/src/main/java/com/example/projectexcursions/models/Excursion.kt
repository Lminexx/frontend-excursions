package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class Excursion(
    val id: Int,
    val title: String,
    val description: String
)