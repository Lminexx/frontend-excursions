package com.example.projectexcursions.repositories.georepo

import com.example.projectexcursions.models.PlaceItem
import com.yandex.mapkit.geometry.Point
import retrofit2.Response

interface GeoRepository {

    suspend fun getRoute(start: Point, end: Point): List<Point>

    fun calculateDistance(p1: Point, p2: Point): Double

    suspend fun getPhotosByLocation(point: Point): List<String>

    fun getRandomId(length: Int): String

    suspend fun uploadPlacesItems(places: List<PlaceItem>, id: Long)

    suspend fun loadPlaces(id: Long): Response<List<PlaceItem>>

    suspend fun updatePoints(places: List<PlaceItem>, id: Long)
}