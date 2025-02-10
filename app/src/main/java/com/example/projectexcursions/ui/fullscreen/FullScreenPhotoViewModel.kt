package com.example.projectexcursions.ui.fullscreen

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FullScreenPhotoViewModel : ViewModel() {

    private val _photos = MutableLiveData<List<Uri>>()
    val photos: LiveData<List<Uri>> get() = _photos

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> get() = _currentPosition

    fun setPhotos(photoList: List<Uri>) {
        _photos.value = photoList
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }
}
