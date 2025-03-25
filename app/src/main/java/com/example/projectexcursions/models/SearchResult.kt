package com.example.projectexcursions.models

import com.yandex.mapkit.geometry.Point

data class SearchResult (
    val id: String,
    val name: String,
    val point: Point
)