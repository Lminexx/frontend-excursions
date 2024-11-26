package com.example.projectexcursions.ui.registration

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.net.ApiClient
import retrofit2.Callback
import com.example.projectexcursions.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class RegViewModel @Inject constructor(
    private val apiClient: ApiClient
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
        Log.d("RegViewModel", "Attempting to register user: $login")
        val user = User(login, password)
        ApiClient.instance.registerUser(user).enqueue(object : Callback<Void> {
            override fun onResponse(call: retrofit2.Call<Void>, response: Response<Void>) {
                Log.d("RegViewModel", "Response received: ${response.code()}")
                if(response.isSuccessful){
                    Log.d("RegViewModel", "User registered successfully!")
                    _regStatus.value = true;
                }else{
                    _regStatus.value=false;
                    Log.e("RegViewModel", "Registration failed: ${response.errorBody()?.string()}")
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