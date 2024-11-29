package com.example.projectexcursions.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor() : ViewModel() {

    private val _menuItem = MutableLiveData<String>()
    val menuItem: LiveData<String> get() = _menuItem

    fun startMainActivity() {
        _menuItem.value = ""
    }

    fun clickExList() {
        _menuItem.value = "list"
    }

    fun clickFav() {
        _menuItem.value = "fav"
    }

    fun clickMap() {
        _menuItem.value = "map"
    }

    fun clickProfile() {
        _menuItem.value = "profile"
    }
}