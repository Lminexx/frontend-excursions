package com.example.projectexcursions.net

import com.example.projectexcursions.models.UserInformation
import com.example.projectexcursions.serializers.BigDecimalSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ExcursionResponse(
    val id: Long,
    val title: String,
    val description: String,
    val userId: Long,
    val user: UserInformation,
    val favorite: Boolean = false,
    @Serializable(with = BigDecimalSerializer::class)
    val rating: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val personalRating: BigDecimal? = null
)