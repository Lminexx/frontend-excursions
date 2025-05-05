package com.example.projectexcursions.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auth0.android.jwt.Claim
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

    private val _wantLogout = MutableLiveData<Boolean>()
    val logout: LiveData<Boolean> get() = _wantLogout

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _goToCreatedExcs = MutableLiveData(false)
    val goToCreatedExcs: LiveData<Boolean> get() = _goToCreatedExcs

    private val _moderateExcursions = MutableLiveData(false)
    val moderateExcursions: LiveData<Boolean> get() = _moderateExcursions

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
            Log.d("DeleteToken", token!!.token)
            repository.deleteToken(token.token)
            Log.d("DeletedToken", token.token)
        }
    }

    fun getDecodeToken(): Map<String, Claim>? {
        val token = repository.getCachedToken()
        return token?.let { repository.decodeToken(it.token) }
    }


    fun clickCreateExcursion() {
        _wantCreate.value = true
    }

    fun isCreating() {
        _wantCreate.value = false
    }

    fun clickComeBack() {
        _wantLogout.value = true
    }

    fun createdExcsList() {
        _goToCreatedExcs.value = true
    }

    fun moderateExcursions() {
        _moderateExcursions.value = true
    }
}