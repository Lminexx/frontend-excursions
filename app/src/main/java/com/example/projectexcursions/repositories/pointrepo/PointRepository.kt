package com.example.projectexcursions.repositories.pointrepo

import com.yandex.mapkit.geometry.Point

interface PointRepository {

    fun cacheStart(point: Point)

    fun cacheEnd(point: Point)

    fun getCachedStart(): Point?

    fun getCachedEnd(): Point?

    fun deleteDCachedStart()

    fun deleteCachedEnd()

    fun deleteCachedPoints()

    fun setRoute(points: List<Point>)

    fun getRoute(): List<Point>?

    fun deleteRoute()

    fun hasRoute(): Boolean

    fun isFirstRoute(): Boolean

    fun setFirst()

    fun setIsntFirst()
}