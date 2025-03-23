package com.example.projectexcursions.models

import android.net.Uri
import com.yandex.mapkit.geometry.Point

data class PlaceItem (
    val name: String,
    val point: Point,
    val photos: List<Uri>
)