package com.example.projectexcursions.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.BuildConfig
import com.example.projectexcursions.models.SearchResult
import com.example.projectexcursions.repositories.georepo.GeoRepository
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
    private val pointRepository: PointRepository,
    private val geoRepository: GeoRepository
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

    suspend fun getRoute() {
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

            val route = geoRepository.getRoute(start, end)
            if (route.isNotEmpty()) {
                pointRepository.setRoute(route)
                _routeLiveData.postValue(route)
            } else {
                _routeFinished.postValue(true)
            }
        } catch (e: Exception) {
            Log.e("Route", "Error getting route", e)
            _routeFinished.postValue(true)
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