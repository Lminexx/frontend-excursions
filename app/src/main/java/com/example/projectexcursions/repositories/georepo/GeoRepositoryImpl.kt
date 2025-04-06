package com.example.projectexcursions.repositories.georepo

import android.util.Log
import com.example.projectexcursions.BuildConfig
import com.example.projectexcursions.models.PlaceItem
import com.example.projectexcursions.net.ApiService
import com.yandex.mapkit.geometry.Point
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GeoRepositoryImpl @Inject constructor(
    private val apiService: ApiService
): GeoRepository {

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

    override fun calculateDistance(p1: Point, p2: Point): Double {
        val lat1 = Math.toRadians(p1.latitude)
        val lon1 = Math.toRadians(p1.longitude)
        val lat2 = Math.toRadians(p2.latitude)
        val lon2 = Math.toRadians(p2.longitude)

        val dlat = lat2 - lat1
        val dlon = lon2 - lon1

        val a = sin(dlat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        Log.d("calculateDistance: ","${63710000 * c}")
        return 6371000 * c
    }

    override suspend fun getPhotosByLocation(point: Point): List<String> {
        val apiKey = BuildConfig.MAPILARY_API_KEY
        val url = "https://graph.mapillary.com/images?fields=id,thumb_1024_url&" +
                "closeto=${point.longitude},${point.latitude}&radius=100&access_token=$apiKey"

        val request = Request.Builder().url(url).build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()?.let { json ->
                    val data = JSONObject(json).optJSONArray("data") ?: return emptyList()
                    List(data.length()) { index ->
                        data.getJSONObject(index).getString("thumb_1024_url")
                    }
                } ?: emptyList()
            } else {
                Log.e("Mapillary", "Ошибка загрузки фото: ${response.message}")
                emptyList()
            }
        } catch (e: IOException) {
            Log.e("Mapillary", "Ошибка загрузки фото", e)
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


    override fun getRandomId(length: Int): String {
        val chars = "0123456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    override suspend fun uploadPlacesItems(places: List<PlaceItem>, id: Long) {
        apiService.uploadPlaceItems(id, places)
    }

    override suspend fun loadPlaces(id: Long): List<PlaceItem> {
        return apiService.loadPlaces(id)
    }
}