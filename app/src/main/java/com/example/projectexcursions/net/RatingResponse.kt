package com.example.projectexcursions.net

import com.example.projectexcursions.serializers.BigDecimalSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class RatingResponse(
    val excursionId: Long,
    val userId:Long,
    @Serializable(with = BigDecimalSerializer::class)
    val ratingValue: BigDecimal
)
