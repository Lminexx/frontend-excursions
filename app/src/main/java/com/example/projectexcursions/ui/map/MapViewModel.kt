package com.example.projectexcursions.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {

    private val _curPoint = MutableLiveData<Point>()
    val curPoint: LiveData<Point> get() = _curPoint

    fun point(point: Point) {
        _curPoint.value = point
    }
}
