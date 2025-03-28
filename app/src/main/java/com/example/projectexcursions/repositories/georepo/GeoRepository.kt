package com.example.projectexcursions.repositories.georepo

import android.net.Uri
import com.yandex.mapkit.geometry.Point
import kotlin.random.Random

interface GeoRepository {

    suspend fun getRoute(start: Point, end: Point): List<Point>

    fun calculateDistance(p1: Point, p2: Point): Double

    suspend fun getPhotosByLocation(point: Point): List<String>

    fun getRandomId(length: Int): String
}