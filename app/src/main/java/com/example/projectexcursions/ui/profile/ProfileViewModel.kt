package com.example.projectexcursions.ui.profile

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.UsernameNotFoundException
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: TokenRepository
    ): ViewModel() {

    private val _username = MutableLiveData<String?>()
    val username: LiveData<String?> get() = _username

    private val _wantCreate = MutableLiveData<Boolean>()
    val wantCreate: LiveData<Boolean> get() = _wantCreate

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    init {
        loadUser()
    }
    private fun loadUser() {
        viewModelScope.launch {
            val token = repository.getCachedToken()
            val decodedToken = token?.let { repository.decodeToken(it.token) }
            val username = decodedToken?.get("username") as? String
            if (!username.isNullOrEmpty()) {
                _username.value = username
            } else {
                _message.value = "Username not found in token"
                //throw UsernameNotFoundException("Username not found in token")
            }
        }
    }

    fun clickCreateExcursion() {
        _wantCreate.value = true
    }

    fun isCreating() {
        _wantCreate.value = false
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }
}