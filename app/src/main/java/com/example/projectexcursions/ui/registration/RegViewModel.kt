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
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
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

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _avatar = MutableLiveData<Uri>()
    val avatar: LiveData<Uri> get() = _avatar

    fun validateAndRegister(context: Context, login: String, password: String, repeatPassword: String) {
        when {
            login.isBlank() -> _validationMessage.value = context.getString(R.string.error_enter_login)
            password.isBlank() -> _validationMessage.value = context.getString(R.string.error_enter_password)
            repeatPassword.isBlank() -> _validationMessage.value = context.getString(R.string.repeat_password)
            password != repeatPassword -> _validationMessage.value = context.getString(R.string.pass_not_same)
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
                    _regRespMes.value = context.getString(R.string.user_registered)
                    _regStatus.value = true
                } else {
                    _regRespMes.value = context.getString(R.string.lang_error)
                }
            } catch (e: Exception) {
                Log.e("RegistrationError", "Ошибка при регистрации: ${e.message}")
                if (e.message!!.contains("409"))
                    _regRespMes.value = context.getString(R.string.user_exists)
                else
                    FirebaseCrashlytics.getInstance().recordException(e)
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

    fun cameBack() {
        _wantComeBack.value = false
    }
}