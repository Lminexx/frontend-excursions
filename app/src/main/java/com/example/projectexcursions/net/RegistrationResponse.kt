package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponse(
    val id: Long,
    val username: String
)