package com.example.projectexcursions.models

import android.net.Uri
import androidx.room.Entity
import com.yandex.mapkit.geometry.Point
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class PlaceItem (
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double
)