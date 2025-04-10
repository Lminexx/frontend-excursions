package com.example.projectexcursions.net


import kotlinx.serialization.Serializable


@Serializable
data class RatingResponse(
    val excursionId: Long,
    val userId:Long,
    val ratingValue: Float,
    val ratingAVG: Float
)
