package com.example.projectexcursions.net

import kotlinx.serialization.Serializable

@Serializable
data class DeleteResponse(
    val isDeleted: Boolean
)