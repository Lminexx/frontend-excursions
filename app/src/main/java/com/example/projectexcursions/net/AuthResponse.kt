package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String?,
    val message: String?
)