package com.example.projectexcursions.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.models.SearchResult
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _curPoint = MutableLiveData<Point>()
    val curPoint: LiveData<Point> get() = _curPoint

    private val _searchResults = MutableLiveData<List<SearchResult>>(emptyList())
    val searchResults: LiveData<List<SearchResult>> get() = _searchResults

    private val _isSearchResultsVisible = MutableLiveData(false)
    val isSearchResultsVisible: LiveData<Boolean> get() = _isSearchResultsVisible

    fun startLocationTracker() {
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
}