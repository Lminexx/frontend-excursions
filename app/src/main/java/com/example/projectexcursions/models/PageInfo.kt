package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class PageInfo(
    val size: Int,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int
)
