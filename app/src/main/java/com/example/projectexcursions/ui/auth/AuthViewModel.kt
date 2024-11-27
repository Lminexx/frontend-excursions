package com.example.projectexcursions.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.net.ApiClient
import com.example.projectexcursions.net.AuthResponse
import com.example.projectexcursions.models.User
import com.example.projectexcursions.token_bd.TokenRepository
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Response
import android.content.Context



class AuthViewModel(private val tokenRepository: TokenRepository) : ViewModel() {


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

    fun validateAndLogin(login: String, password: String) {
        when {
            login.isBlank() -> _validationMessage.value = "Введите логин"
            password.isBlank() -> _validationMessage.value = "Введите пароль"
            else -> {
                _validationMessage.value = null
                login(login, password)
            }
        }
    }

    fun login(login: String, password: String) {
        val user = User(login, password)
        ApiClient.instance.authUser(user).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: retrofit2.Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    _token.value = token
                    tokenRepository.saveToken(token)
                    _loginStatus.value = true
                } else {
                    _loginStatus.value = false
                    _message.value = response.message() ?: "Ошибка авторизации"
                }
            }

            override fun onFailure(call: retrofit2.Call<AuthResponse>, t: Throwable) {
                _loginStatus.value = false
                _message.value = t.localizedMessage ?: "Ошибка сети"
            }
        })
    }
    fun checkAndDecodeToken(): LiveData<Map<String, Any>?> {
        val tokenInfo = MutableLiveData<Map<String, Any>?>()

        viewModelScope.launch {
            val token = tokenRepository.getToken()
            if (token != null && tokenRepository.isTokenValid(token)) {
                val decodedClaims = tokenRepository.decodeToken(token)
                tokenInfo.postValue(decodedClaims)
            } else {
                tokenInfo.postValue(null)
            }
        }

        return tokenInfo
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