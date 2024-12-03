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
import retrofit2.Call



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
    val validationMessage: LiveData<String?> get() = _validationMessage

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

    private fun login(login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = User(login, password)

                val response = ApiClient.instance.authUser(user)

                if (response.isSuccessful && response.body() != null) {
                    val tokenValue = response.body()?.token
                    if (tokenValue != null) {
                        _token.value = tokenValue

                        // Сохранение токена с помощью suspend функции репозитория.
                        tokenRepository.saveToken(tokenValue)
                        _loginStatus.value = true
                    } else {
                        handleError("Ошибка: Токен не получен")
                    }

                } else {
                    handleError(response.message())
                }
            } catch (e: Exception) {
                handleError(e.localizedMessage ?: "Ошибка сети")
            }
        }
    }

    private fun handleError(message: String?) {
        _loginStatus.value = false
        _message.value = message ?: "Ошибка авторизации"
    }

    fun checkAndDecodeToken(): LiveData<Map<String, Any>?> {
        val tokenInfo = MutableLiveData<Map<String, Any>?>()

        viewModelScope.launch {
            val tokenValue = tokenRepository.getToken()
            if (tokenValue != null && tokenRepository.isTokenValid(tokenValue)) {
                tokenInfo.postValue(tokenRepository.decodeToken(tokenValue))
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
