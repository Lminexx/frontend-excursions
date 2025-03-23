package com.example.projectexcursions.repositories.georepo

import com.yandex.mapkit.geometry.Point

interface GeoRepository {

    suspend fun getRoute(start: Point, end: Point): List<Point>

    fun calculateDistance(p1: Point, p2: Point): Double
}