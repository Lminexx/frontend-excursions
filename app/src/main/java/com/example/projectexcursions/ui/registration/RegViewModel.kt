package com.example.projectexcursions.ui.registration

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Callback
import com.example.projectexcursions.models.User
import com.example.projectexcursions.net.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RegViewModel @Inject constructor(
    private val apiService: ApiService
): ViewModel() {

    private val _validationMessage = MutableLiveData<String?>()
    val validationMessage: LiveData<String?> get() = _validationMessage

    private val _regStatus = MutableLiveData<Boolean>()
    val regStatus: LiveData<Boolean> get() = _regStatus

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    fun validateAndRegister(login: String, password: String, repeatPassword: String) {
        when {
            login.isBlank() -> _validationMessage.value = "Введите логин"
            password.isBlank() -> _validationMessage.value = "Введите пароль"
            repeatPassword.isBlank() -> _validationMessage.value = "Повторите пароль"
            password != repeatPassword -> _validationMessage.value = "Пароли не совпадают"
            else -> {
                _validationMessage.value = null
                reg(login, password)
            }
        }
    }

    fun reg(login: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = User(login, password)
                val response = apiService.registerUser(user)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.code == 200) {
                            Log.d("RegViewModel", "User registered successfully")
                            _regStatus.value = true
                        } else {
                            Log.e("RegViewModel", "Registration failed: ${body?.code}")
                            _regStatus.value = false
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Log.e("RegViewModel", "Server error: $errorMessage")
                        _regStatus.value = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("RegViewModel", "Network error", e)
                    _regStatus.value = false
                }
            }
        }
    }

    fun clickRegButton() {
        _regStatus.value = true
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }
}