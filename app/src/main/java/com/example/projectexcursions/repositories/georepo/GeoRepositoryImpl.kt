package com.example.projectexcursions.repositories.georepo

import android.util.Log
import com.example.projectexcursions.BuildConfig
import com.yandex.mapkit.geometry.Point
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class GeoRepositoryImpl: GeoRepository {

    private val client = OkHttpClient()

    override suspend fun getRoute(start: Point, end: Point): List<Point> {
        val apiKey = BuildConfig.GHOPPER_API_KEY
        val url = "https://graphhopper.com/api/1/route?point=${start.latitude},${start.longitude}" +
                "&point=${end.latitude},${end.longitude}&vehicle=foot&locale=ru&key=$apiKey"

        val request = Request.Builder().url(url).build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { json ->
                    val route = JSONObject(json).getJSONArray("paths").getJSONObject(0)
                    val pointsField = route.get("points")
                    if (pointsField is String) {
                        Log.d("decodePoly", "else-if, $pointsField")
                        decodePolyline(pointsField)
                    } else {
                        Log.d("decodePoly", "emptyList()")
                        emptyList()
                    }
                } ?: emptyList()
            } else {
                Log.e("GraphHopper", "Ошибка загрузки маршрута: ${response.message}")
                emptyList()
            }
        } catch (e: IOException) {
            Log.e("GraphHopper", "Ошибка загрузки маршрута", e)
            emptyList()
        }
    }

    private fun decodePolyline(encoded: String): List<Point> {
        Log.d("encoded", encoded)
        val poly = mutableListOf<Point>()
        var index = 0
        var lat = 0
        var lng = 0
        while (index < encoded.length) {
            var result = 0
            var shift = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) -(result shr 1) else (result shr 1)
            lat += dlat

            result = 0
            shift = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) -(result shr 1) else (result shr 1)
            lng += dlng

            val latitude = lat / 1e5
            val longitude = lng / 1e5
            poly.add(Point(latitude, longitude))
        }
        Log.d("poly", "${poly.isNotEmpty()}, ${poly[1].longitude}, ${poly[1].latitude}")
        return poly
    }
}