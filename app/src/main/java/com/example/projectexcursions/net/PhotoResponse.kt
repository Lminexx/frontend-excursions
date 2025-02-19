package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class PhotoResponse(
    val id: Long,
    val url: String,
    val excursionId: Long
)
