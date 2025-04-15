package com.example.projectexcursions.ui.registration

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.R
import com.example.projectexcursions.models.User
import com.example.projectexcursions.net.ApiService
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegViewModel @Inject constructor(
    private val apiService: ApiService
): ViewModel() {

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    private val _regStatus = MutableLiveData<Boolean>()
    val regStatus: LiveData<Boolean> get() = _regStatus

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _avatar = MutableLiveData<Uri>()
    val avatar: LiveData<Uri> get() = _avatar

    fun validateAndRegister(context: Context, login: String, password: String, repeatPassword: String) {
        when {
            login.isBlank() -> _message.value = context.getString(R.string.error_enter_login)
            password.isBlank() -> _message.value = context.getString(R.string.error_enter_password)
            repeatPassword.isBlank() -> _message.value = context.getString(R.string.repeat_password)
            password != repeatPassword -> _message.value = context.getString(R.string.pass_not_same)
            else -> {
                _username.value = login
                _password.value = password
                reg(context)
            }
        }
    }

    private fun reg(context: Context) {
        viewModelScope.launch {
            try {
                val username = _username.value!!
                val password = _password.value!!
                if (isInputLangValid(username) && isInputLangValid(password)) {
                    val user = User(username, password)
                    val response = apiService.registerUser(user)
                    Log.d("RegistrationResponse", "Response: $response")
                    _message.value = context.getString(R.string.user_registered)
                    _regStatus.value = true
                } else {
                    _message.value = context.getString(R.string.lang_error)
                }
            } catch (e: Exception) {
                Log.e("RegistrationError", "Ошибка при регистрации: ${e.message}")
                if (e.message!!.contains("409"))
                    _message.value = context.getString(R.string.user_exists)
                else {
                    _message.value = e.message
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    private fun isInputLangValid(input: String): Boolean {
        val regex = "^[a-zA-Z0-9!@#\$%^&*()_+{}\\[\\]:;<>,.?~\\-=\\s]*\$".toRegex()
        return regex.matches(input)
    }

    fun addProfilePicture(image: Uri){
        _avatar.value=image
    }
}