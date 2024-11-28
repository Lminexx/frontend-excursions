package com.example.projectexcursions.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.models.User
import com.example.projectexcursions.net.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegViewModel @Inject constructor(
    private val apiService: ApiService
): ViewModel() {

    private val _validationMessage = MutableLiveData<String?>()
    val validationMessage: LiveData<String?> get() = _validationMessage

    private val _regRespMes = MutableLiveData<String?>()
    val regRespMes: LiveData<String?> get() = _regRespMes

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

    private fun reg(login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = User(login, password)
                val response = apiService.registerUser(user)

                if (response.isSuccessful) {
                    val body = response.body()
                    when (body?.code) {
                        200 -> _regRespMes.value = "Пользователь зарегестрирован"
                        400 -> _regRespMes.value = "Неверный запрос"
                        429 -> _regRespMes.value = "Слишком много запросов за определённый промежуток времени"
                        500 -> _regRespMes.value = "Проблема на стороне сервера"
                    }
                } else {
                    _regRespMes.value = "нет подключения"
                }
            } catch (e: Exception) {
                _regRespMes.value = "Ошибка: \n$e"
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