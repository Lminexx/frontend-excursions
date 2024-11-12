package com.example.projectexcursions.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.net.ApiClient
import retrofit2.Callback
import com.example.projectexcursions.user.User
import retrofit2.Response

class RegViewModel: ViewModel() {
    //TODO обзяательная проверка логина и пароля, если такие существуют - выдать исключение с сообщшением или просто сообщение с ошибкой

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
        val user = User(login, password)
        ApiClient.instance.registerUser(user).enqueue(object : Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: Response<Void>) {
                when(response.isSuccessful) {
                    true -> _regStatus.value = true
                    false -> _regStatus.value = false
                }
            }

            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
                _regStatus.value = false
            }
        })
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