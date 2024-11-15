package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
)