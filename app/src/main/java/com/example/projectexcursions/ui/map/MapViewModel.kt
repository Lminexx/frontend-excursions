package com.example.projectexcursions.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.BuildConfig
import com.example.projectexcursions.models.SearchResult
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import dagger.hilt.android.lifecycle.HiltViewModel
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
class MapViewModel @Inject constructor() : ViewModel() {

    private val _curPoint = MutableLiveData<Point?>()
    val curPoint: LiveData<Point?> get() = _curPoint

    private val _endPoint = MutableLiveData<Point>()
    val endPoint: LiveData<Point> get() = _endPoint

    private val _routeLiveData = MutableLiveData<List<Point>>()
    val routeLiveData: LiveData<List<Point>> = _routeLiveData

    private val _searchResults = MutableLiveData<List<SearchResult>>(emptyList())
    val searchResults: LiveData<List<SearchResult>> get() = _searchResults

    private val _isSearchResultsVisible = MutableLiveData(false)
    val isSearchResultsVisible: LiveData<Boolean> get() = _isSearchResultsVisible

    private val _routeFinished = MutableLiveData(true)
    val routeFinished: LiveData<Boolean> get() = _routeFinished

    private val _routeEnded = MutableLiveData<Boolean>()
    val routeEnded: LiveData<Boolean> get() = _routeEnded

    private val locationListener = object : LocationListener {
        override fun onLocationUpdated(location: Location) {
            Log.d("startLocationTracker", "onLocationUpdated")
            if (_curPoint != endPoint)
                _curPoint.value = location.position
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

    private val locationManager = MapKitFactory.getInstance().createLocationManager()

    fun getUserLocation() {
        val locationManager = MapKitFactory.getInstance().createLocationManager()
        locationManager.requestSingleUpdate(object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                Log.d("startLocationTracker", "onLocationUpdated")
                _curPoint.value = location.position
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                Log.d("startLocationTracker", "onLocationStatusUpdated")
                when (locationStatus) {
                    LocationStatus.NOT_AVAILABLE -> Log.e("Location", "Not available")
                    LocationStatus.AVAILABLE -> Log.d("Location", "Available")
                    LocationStatus.RESET -> Log.d("Location", "Reset")
                }
            }
        })
    }

    fun startLocationTracker() {
        Log.d("endPoint", "${endPoint.value?.latitude}, ${endPoint.value?.longitude}")
        Log.d("curPoint", "${curPoint.value?.latitude}, ${curPoint.value?.longitude}")

        locationManager.subscribeForLocationUpdates(
            5.0,
            6000,
            0.9,
            true,
            FilteringMode.OFF,
            Purpose.PEDESTRIAN_NAVIGATION,
            locationListener)
        checkRouteCompletion(curPoint.value!!, endPoint.value!!)
    }

    fun getRoute() {
        Log.d("endPoint", "${endPoint.value?.latitude}, ${endPoint.value?.longitude}")
        Log.d("curPoint", "${curPoint.value?.latitude}, ${curPoint.value?.longitude}")
        try {
            _routeFinished.value = false
            val end = _endPoint.value!!
            val start = _curPoint.value!!
            Log.d("end", end.toString())
            Log.d("start", start.toString())
            val apiKey = BuildConfig.GHOPPER_API_KEY
            val url =
                "https://graphhopper.com/api/1/route?point=${start.latitude},${start.longitude}" +
                        "&point=${end.latitude},${end.longitude}&vehicle=car&locale=ru&key=$apiKey"

            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("GraphHopper", "Ошибка загрузки маршрута", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { json ->
                        val points = parseRoute(json)
                        _routeLiveData.postValue(points)
                    }
                }
            })
        } catch (nullException: NullPointerException) {
            Log.d("Point", "Null")
        }
    }

    private fun parseRoute(json: String): List<Point> {
        val route = JSONObject(json).getJSONArray("paths").getJSONObject(0)
        val coordsArray = route.getJSONObject("points").getJSONArray("coordinates")

        return (0 until coordsArray.length()).map {
            val coord = coordsArray.getJSONArray(it)
            Point(coord.getDouble(1), coord.getDouble(0))
        }
    }

    private fun checkRouteCompletion(userLocation: Point, end: Point) {
        if (routeLiveData.value != null) {
            val distance = calculateDistance(userLocation, end)

            if (distance < 10 && !routeFinished.value!!)
                endRoute()
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

    fun endRoute() {
        _routeFinished.value = true
        _routeEnded.value = true
    }

    fun setEndPoint(point: Point) {
        _endPoint.value = point
        Log.d("endPoint", "${endPoint.value?.latitude}, ${endPoint.value?.longitude}")
    }
}