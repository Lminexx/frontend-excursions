package com.example.projectexcursions.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(
    private val repository: TokenRepository
) : ViewModel() {

    private val _isAuth = MutableLiveData<Boolean>()
    val isAuth: LiveData<Boolean> get() = _isAuth

    private val _menuItem = MutableLiveData<String?>()
    val menuItem: LiveData<String?> get() = _menuItem

    fun setStartFragment() {
        _menuItem.value = null
    }

    fun changeMenuItem(newMenuItem: String?) {
        Log.d("MenuItemHasChanged", newMenuItem.toString())
        _menuItem.value = newMenuItem
        Log.d("MenuItem", "${_menuItem.value}")
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = repository.getToken()
        return token != null
    }
}