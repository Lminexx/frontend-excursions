package com.example.projectexcursions.net

import com.example.projectexcursions.models.UserInformation
import kotlinx.serialization.Serializable

@Serializable
data class ExcursionResponse(
    val id: Long,
    val title: String,
    val description: String,
    val userId: Long,
    val user: UserInformation,
    val favorite: Boolean = false
)