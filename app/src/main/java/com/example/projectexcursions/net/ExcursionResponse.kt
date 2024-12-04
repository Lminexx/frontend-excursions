package com.example.projectexcursions.net

import com.example.projectexcursions.models.Excursion
import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse (
    val excursion: Excursion
)