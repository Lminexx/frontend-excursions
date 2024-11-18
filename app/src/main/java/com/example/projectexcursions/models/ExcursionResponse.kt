package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse(
    val content: List<Excursion>
)