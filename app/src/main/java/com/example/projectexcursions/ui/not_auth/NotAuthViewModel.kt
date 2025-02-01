package com.example.projectexcursions.ui.not_auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotAuthViewModel @Inject constructor(): ViewModel() {

    private val _wantAuth = MutableLiveData<Boolean>()
    val wantAuth: LiveData<Boolean> get() = _wantAuth

    fun wantAuth() {
        _wantAuth.value = true
    }
}