package com.example.projectexcursions.ui.map

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.BuildConfig
import com.example.projectexcursions.models.SearchResult
import com.example.projectexcursions.repositories.pointrepo.PointRepository
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import dagger.hilt.android.lifecycle.HiltViewModel
import hilt_aggregated_deps._com_example_projectexcursions_ui_registration_RegViewModel_HiltModules_BindsModule
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class MapViewModel @Inject constructor(
    private val pointRepository: PointRepository
) : ViewModel() {

    private val _curPoint = MutableLiveData<Point?>()
    val curPoint: LiveData<Point?> get() = _curPoint

    private val _endPoint = MutableLiveData<Point>()
    val endPoint: LiveData<Point> get() = _endPoint

    private val _routeLiveData = MutableLiveData<List<Point>>(emptyList())
    val routeLiveData: LiveData<List<Point>> get() = _routeLiveData

    private val _searchResults = MutableLiveData<List<SearchResult>>(emptyList())
    val searchResults: LiveData<List<SearchResult>> get() = _searchResults

    private val _isSearchResultsVisible = MutableLiveData(false)
    val isSearchResultsVisible: LiveData<Boolean> get() = _isSearchResultsVisible

    private val _routeFinished = MutableLiveData(true)
    val routeFinished: LiveData<Boolean> get() = _routeFinished

    private val _routeEnded = MutableLiveData(false)
    val routeEnded: LiveData<Boolean> get() = _routeEnded

    private val _userLocationOnRoute = MutableLiveData<Point?>()
    val userLocationOnRoute: LiveData<Point?> get() = _userLocationOnRoute

    private var locationManager: LocationManager? = null


    private val locationListener = object : LocationListener {
        override fun onLocationUpdated(location: Location) {
            Log.d("startLocationTracker", "onLocationUpdated")
            if (pointRepository.hasRoute()) {
                _userLocationOnRoute.postValue(location.position)
                pointRepository.cacheStart(location.position)
                checkRouteCompletion(location.position, pointRepository.getCachedEnd()!!)
            }
        }

        override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
            Log.d("startLocationTracker", "onLocationStatusUpdated")
            when (locationStatus) {
                LocationStatus.NOT_AVAILABLE -> Log.e("Location", "Not available")
                LocationStatus.AVAILABLE -> Log.d("Location", "Available")
                LocationStatus.RESET -> Log.d("Location", "Reset")
            }
        }
    }

    fun getUserLocation() {
        _curPoint.value = null
        locationManager = MapKitFactory.getInstance().createLocationManager()
        locationManager?.requestSingleUpdate(object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                Log.d("getUserLocation", "onLocationUpdated")
                _curPoint.postValue(location.position)
                pointRepository.cacheStart(location.position)
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                Log.d("getUserLocation", "onLocationStatusUpdated")
                when (locationStatus) {
                    LocationStatus.NOT_AVAILABLE -> Log.e("Location", "Not available")
                    LocationStatus.AVAILABLE -> Log.d("Location", "Available")
                    LocationStatus.RESET -> Log.d("Location", "Reset")
                }
            }
        })
    }

    fun startLocationTracker() {
        locationManager = MapKitFactory.getInstance().createLocationManager()
        locationManager?.subscribeForLocationUpdates(
            5.0,
            6000,
            0.0,
            true,
            FilteringMode.OFF,
            Purpose.PEDESTRIAN_NAVIGATION,
            locationListener
        )
    }

    fun getRoute() {
        try {
            val end = pointRepository.getCachedEnd()
            val start = pointRepository.getCachedStart()
            _curPoint.postValue(start)
            if (end == null || start == null) {
                Log.d("Point", "Start or end point is null")
                return
            }

            _routeFinished.postValue(false)
            _routeEnded.postValue(false)

            val apiKey = BuildConfig.GHOPPER_API_KEY
            val url =
                "https://graphhopper.com/api/1/route?point=${start.latitude},${start.longitude}" +
                        "&point=${end.latitude},${end.longitude}&vehicle=car&locale=ru&key=$apiKey"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("GraphHopper", "Ошибка загрузки маршрута", e)
                    _routeFinished.postValue(true)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { json ->
                        val points = parseRoute(json)
                        if (points.isNotEmpty()) {
                            pointRepository.setRoute(points)
                            _routeLiveData.postValue(points)
                        } else {
                            _routeFinished.postValue(true)
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("Route", "Error getting route", e)
            _routeFinished.postValue(true)
        }
    }

    private fun parseRoute(json: String): List<Point> {
        val route = JSONObject(json).getJSONArray("paths").getJSONObject(0)
        val pointsField = route.get("points")
        Log.d("parse", "true)")
        return if (pointsField is JSONObject) {
            val encoded = pointsField.getBoolean("encoded")
            if (!encoded) {
                val coordsArray = pointsField.getJSONArray("coordinates")
                Log.d("coordsArray", "${coordsArray.equals(null)}")
                (0 until coordsArray.length()).map {
                    val coord = coordsArray.getJSONArray(it)
                    Point(coord.getDouble(1), coord.getDouble(0))
                }
            } else {
                val polylineStr = pointsField.getString("points")
                Log.d("polylineStr", polylineStr)
                decodePoly(polylineStr)
            }
        } else if (pointsField is String) {
            Log.d("decodePoly", "else-if, $pointsField")
            decodePoly(pointsField)
        } else {
            Log.d("decodePoly", "emptyList()")
            emptyList()
        }
    }

    private fun checkRouteCompletion(userLocation: Point, end: Point) {
        if (pointRepository.hasRoute()) {
            val distance = calculateDistance(userLocation, end)
            if (distance < 10) {
                endRoute()
            }
        }
    }

    private fun calculateDistance(p1: Point, p2: Point): Double {
        val lat1 = Math.toRadians(p1.latitude)
        val lon1 = Math.toRadians(p1.longitude)
        val lat2 = Math.toRadians(p2.latitude)
        val lon2 = Math.toRadians(p2.longitude)

        val dlat = lat2 - lat1
        val dlon = lon2 - lon1

        val a = sin(dlat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return 6371000 * c
    }

    private fun decodePoly(encoded: String): List<Point> {
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

    fun updateSearchResults(results: List<SearchResult>) {
        _searchResults.value = results
        _isSearchResultsVisible.value = results.isNotEmpty()
    }

    fun deleteSearchResults() {
        _searchResults.value = emptyList()
        _isSearchResultsVisible.value = false
    }

    fun toggleSearchResultsVisibility() {
        _isSearchResultsVisible.value = _isSearchResultsVisible.value?.not()
    }

    fun hideSearchResults() {
        _isSearchResultsVisible.value = false
    }

    fun deleteUserPos() {
        _curPoint.value = null
    }

    fun getUserPos(): Point? {
        return curPoint.value
    }

    fun endRoute() {
        _routeFinished.value = true
        _routeEnded.postValue(true)
        pointRepository.deleteRoute()
        getUserLocation()
    }

    fun setEndPoint(point: Point) {
        _endPoint.value = point
        pointRepository.cacheEnd(point)
        Log.d("endPoint", "${endPoint.value?.latitude}, ${endPoint.value?.longitude}")
    }
}