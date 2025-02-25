package com.example.projectexcursions.ui.map

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import com.yandex.mapkit.location.Purpose
import com.yandex.mapkit.map.VisibleRegion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.logging.Filter
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _curPoint = MutableLiveData<Point>()
    val curPoint: LiveData<Point> get() = _curPoint

    private val region = MutableStateFlow<VisibleRegion?>(null)


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

    fun setVisibleRegion(region: VisibleRegion) {
        this.region.value = region
    }
}