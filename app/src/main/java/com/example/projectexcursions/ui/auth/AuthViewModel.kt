package com.example.projectexcursions.ui.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.R
import com.example.projectexcursions.models.User
import com.example.projectexcursions.net.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService
): ViewModel() {

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> get() = _loginStatus

    private val _wantReg = MutableLiveData<Boolean>()
    val wantReg: LiveData<Boolean> get() = _wantReg

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> get() = _token

    private val _validationMessage = MutableLiveData<String?>()
    val value : LiveData<String?> get() = _validationMessage

    fun validateAndLogin(context: Context, login: String, password: String) {
        when {
            login.isBlank() -> _validationMessage.value = context.getString(R.string.error_enter_login)
            password.isBlank() -> _validationMessage.value = context.getString(R.string.error_enter_password)
            else -> {
                _validationMessage.value = null
                login(context, login, password)
            }
        }
    }

    private fun login(context: Context, login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = User(login, password)
                val response = apiService.authUser(user)
                _token.value = response.token
                _loginStatus.value = true
            } catch (e: retrofit2.HttpException) {
                _loginStatus.value = false
                _message.value = when (e.code()) {
                    401 -> context.getString(R.string.error_auth)
                    403 -> context.getString(R.string.error_auth)
                    else -> context.getString(R.string.error_auth)
                }
            } catch (e: Exception) {
                _loginStatus.value = false
                _message.value = e.localizedMessage ?: context.getString(R.string.error_net)
            }
        }
    }

    fun clickAuth() {
        _loginStatus.value = true
    }

    fun sucAuth() {
        _loginStatus.value = false
    }

    fun clickRegister() {
        _wantReg.value = true
    }

    fun goneToReg() {
        _wantReg.value = false
    }
}