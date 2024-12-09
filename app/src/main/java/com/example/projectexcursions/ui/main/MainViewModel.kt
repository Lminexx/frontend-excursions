package com.example.projectexcursions.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class MainViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _menuItem = MutableLiveData<String?>()
    val menuItem: LiveData<String?> get() = _menuItem

    private val _isAuth = MutableLiveData<Boolean>()

    val isAuth: LiveData<Boolean> get() = _isAuth

    fun startMainActivity() {
        _menuItem.value = null
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

    suspend fun checkAuthStatus() {
        val token = tokenRepository.getToken()
        _isAuth.value = token != null
    }
}