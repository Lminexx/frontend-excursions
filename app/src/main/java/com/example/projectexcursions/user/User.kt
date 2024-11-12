package com.example.projectexcursions.user

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val login: String,
    val password: String
)

@Serializable
data class RegistrationResponse(
    val success: Boolean,
    val message: String,
    val userId: Int? = null
)