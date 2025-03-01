package com.example.projectexcursions.repositories.pointrepo

import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

class PointRepositoryImpl @Inject constructor(): PointRepository {

    private var cachedStart: Point? = null

    private var cachedEnd: Point? = null

    private var cachedRoute: List<Point>? = null

    override fun cacheStart(point: Point) {
        cachedStart = point
    }

    override fun cacheEnd(point: Point) {
        cachedEnd = point
    }

    override fun getCachedStart(): Point? {
        return cachedStart
    }

    override fun getCachedEnd(): Point? {
        return cachedEnd
    }

    override fun deleteDCachedStart() {
        cachedStart = null
    }

    override fun deleteCachedEnd() {
        cachedEnd = null
    }

    override fun deleteCachedPoints() {
        cachedEnd = null
        cachedStart = null
    }

    override fun setRoute(points: List<Point>) {
        cachedRoute = points
    }

    override fun getRoute(): List<Point>? {
        return cachedRoute!!
    }

    override fun deleteRoute() {
        cachedRoute = null
    }

    override fun hasRoute(): Boolean {
        return cachedRoute?.equals(null) ?: false
    }
}