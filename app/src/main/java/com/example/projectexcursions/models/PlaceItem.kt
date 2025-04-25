package com.example.projectexcursions.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaceItem (
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double
)