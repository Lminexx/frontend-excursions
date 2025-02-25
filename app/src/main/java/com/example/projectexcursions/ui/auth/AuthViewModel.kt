package com.example.projectexcursions.ui.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.R
import com.example.projectexcursions.models.Token
import com.example.projectexcursions.models.User
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
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
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenRepository: TokenRepository
): ViewModel() {

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> get() = _loginStatus

    private val _wantReg = MutableLiveData<Boolean>()
    val wantReg: LiveData<Boolean> get() = _wantReg

    private val _token = MutableLiveData<String>()
    val token: LiveData<String> get() = _token

    private val _validationMessage = MutableLiveData<String>()
    val validationMessage: LiveData<String> get() = _validationMessage

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _avatar = MutableLiveData<Uri>()
    val avatar: LiveData<Uri> get() = _avatar

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                tempFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        }catch (e: IOException){
            Log.e("getFileFromUri", "Error copying file from URI: $uri", e)
            return tempFile
        }
    }

    fun validateAndLogin(context: Context, login: String, password: String) {
        when {
            login.isBlank() -> _validationMessage.value = context.getString(R.string.error_enter_login)
            password.isBlank() -> _validationMessage.value = context.getString(R.string.error_enter_password)
            !isInputLangValid(login) -> _validationMessage.value = context.getString(R.string.lang_error)
            !isInputLangValid(password) -> _validationMessage.value = context.getString(R.string.lang_error)
            else -> {
                checkLogin(context, login, password)
            }
        }
    }

    private fun checkLogin(context: Context, login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = User(login, password)
                val response = apiService.authUser(user)
                _token.value = response.token
                tokenRepository.saveToken(Token(token = token.value!!))
                Log.d("CachedToken", "${tokenRepository.getCachedToken()}")
                _loginStatus.value = true
                _avatar.value?.let { uploadAvatar(context, it) }
            } catch (e: retrofit2.HttpException) {
                _loginStatus.value = false
                val errorMessage = when (e.code()) {
                    401, 403 -> context.getString(R.string.error_auth)
                    else -> context.getString(R.string.error_auth)
                }
                _validationMessage.value = errorMessage
            } catch (e: Exception) {
                _loginStatus.value = false
                _validationMessage.value = e.localizedMessage
                Log.e("LoginError", "Login error: ", e)
            }
        }
    }

    private fun uploadAvatar(context: Context, uri: Uri){
        viewModelScope.launch {
            try {
                val file = getFileFromUri(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody =
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                val fileName = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
                tokenRepository.uploadAvatar(uri, fileName, multipartBody, apiService)
                Log.d("Avatar", "AvatarUpload")
            } catch (e: Exception) {
                Log.e("PhotoUploadError", "Error uploading photo: ${e.message}")
            }
        }
    }

    private fun isInputLangValid(input: String): Boolean {
        val regex = "^[a-zA-Z0-9!@#\$%^&*()_+{}\\[\\]:;<>,.?~\\-=\\s]*\$".toRegex()
        return regex.matches(input)
    }

    fun setAvatar(uri: Uri){
        _avatar.value=uri
    }

    fun clickRegister() {
        _wantReg.value = true
    }

    fun goneToReg() {
        _wantReg.value = false
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }
}