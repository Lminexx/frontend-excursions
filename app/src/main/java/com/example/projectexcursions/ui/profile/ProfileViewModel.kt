package com.example.projectexcursions.ui.profile

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    //временная заглушка
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _goToCreatedExcs = MutableLiveData(false)
    val goToCreatedExcs: LiveData<Boolean> get() = _goToCreatedExcs

    init {
        loadUser()
    }
    private fun loadUser() {
        viewModelScope.launch {
            val token = repository.getCachedToken()
            val decodedToken = token?.let { repository.decodeToken(it.token) }
            val username = decodedToken?.get("username")?.asString()
            if (!username.isNullOrEmpty()) {
                _username.value = username
                Log.d("UsernameCheck1", username)
                Log.d("UsernameCheck2", _username.value!!)
            } else {
                _message.value = "Username not found in token"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            Log.d("DeleteToken", "true")
            val token = repository.getCachedToken()
            repository.deleteToken(token!!.token)
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

    fun createdExcsList() {
        _goToCreatedExcs.value = true
    }
}