package com.example.projectexcursions.repositories.pointrepo

import android.util.Log
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject

class PointRepositoryImpl @Inject constructor(): PointRepository {

    private var cachedStart: Point? = null

    private var cachedEnd: Point? = null

    private var cachedRoute: List<Point>? = null

    override fun cacheStart(point: Point) {
        cachedStart = point
        Log.d("cachedStart", "${cachedStart?.longitude}, ${cachedStart?.latitude}")
    }

    override fun cacheEnd(point: Point) {
        cachedEnd = point
        Log.d("cachedEnd", "${cachedEnd?.longitude}, ${cachedEnd?.latitude}")
    }

    override fun getCachedStart(): Point? {
        Log.d("cachedStart", "${cachedStart?.longitude}, ${cachedStart?.latitude}")
        return cachedStart
    }

    override fun getCachedEnd(): Point? {
        Log.d("cachedEnd", "${cachedEnd?.longitude}, ${cachedEnd?.latitude}")
        return cachedEnd
    }

    override fun deleteDCachedStart() {
        cachedStart = null
        Log.d("cachedStart", "${cachedStart?.longitude}, ${cachedStart?.latitude}")
    }

    override fun deleteCachedEnd() {
        cachedEnd = null
        Log.d("cachedEnd", "${cachedEnd?.longitude}, ${cachedEnd?.latitude}")
    }

    override fun deleteCachedPoints() {
        cachedEnd = null
        cachedStart = null
        Log.d("cachedStart", "${cachedStart?.longitude}, ${cachedStart?.latitude}")
        Log.d("cachedEnd", "${cachedEnd?.longitude}, ${cachedEnd?.latitude}")
    }

    override fun setRoute(points: List<Point>) {
        cachedRoute = points
        Log.d("cachedRoute 1 Point", "${cachedRoute?.get(1)?.longitude.toString()}, ${cachedRoute?.get(1)?.latitude.toString()}")
        Log.d("cachedRoute Last Point", "${cachedRoute?.get(cachedRoute!!.lastIndex)?.longitude.toString()}, ${cachedRoute?.get(cachedRoute!!.lastIndex)?.latitude.toString()}")
    }

    override fun getRoute(): List<Point>? {
        return cachedRoute!!
    }

    override fun deleteRoute() {
        cachedRoute = null
    }

    override fun hasRoute(): Boolean {
        return cachedRoute != null
    }
}