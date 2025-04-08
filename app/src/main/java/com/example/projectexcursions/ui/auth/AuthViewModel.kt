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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val apiService: ApiService,
    private val tokenRepository: TokenRepository
) : ViewModel() {

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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _role = MutableLiveData<String?>()
    val role: LiveData<String?> get() = _role


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
        } catch (e: IOException) {
            Log.e("getFileFromUri", "Error copying file from URI: $uri", e)
            throw e
        }
    }

    fun validateAndLogin(context: Context, login: String, password: String) {
        when {
            login.isBlank() -> _validationMessage.value =
                context.getString(R.string.error_enter_login)

            password.isBlank() -> _validationMessage.value =
                context.getString(R.string.error_enter_password)

            !isInputLangValid(login) -> _validationMessage.value =
                context.getString(R.string.lang_error)

            !isInputLangValid(password) -> _validationMessage.value =
                context.getString(R.string.lang_error)

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
                _avatar.value?.let { avatarUri ->
                    try {
                        uploadAvatar(context, avatarUri)
                    } catch (e: Exception) {
                        Log.e("AvatarUploadError", "Failed to upload avatar: ${e.message}", e)
                    }
                }
                viewModelScope.launch {
                    val token = tokenRepository.getCachedToken()
                    val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
                    val userRole = decodedToken?.get("role")?.asString()
                    _role.value = userRole
                    Log.d("UserRoleCheck1", userRole ?: "NULL")
                    Log.d("UserRoleCheck2", _role.value!!)
                }
                _loginStatus.value = true
            } catch (e: HttpException) {
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

    private suspend fun uploadAvatar(context: Context, uri: Uri) {
        _isLoading.value = true
        try {
            val file = getFileFromUri(context, uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody =
                MultipartBody.Part.createFormData("file", file.name, requestFile)
            val fileName = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val response = apiService.uploadAvatar(fileName, multipartBody)
            _token.postValue(response.token)
            tokenRepository.clearToken()
            tokenRepository.saveToken(Token(token = response.token))
            Log.d("Avatar", "Avatar uploaded successfully")
            Log.d("CachedToken", "${tokenRepository.getCachedToken()}")
        } catch (e: IOException) {
            Log.e("PhotoUploadError", "IO Error uploading photo: ${e.message}", e)
            _validationMessage.postValue("Ошибка при чтении файла: ${e.message}")
            throw e
        } catch (e: HttpException) {
            Log.e(
                "PhotoUploadError",
                "HTTP Error uploading photo: ${e.message}, code: ${e.code()}",
                e
            )
            _validationMessage.postValue("Ошибка при загрузке: ${e.code()}")
            throw e
        } catch (e: CancellationException) {
            Log.e("PhotoUploadError", "Upload cancelled: ${e.message}", e)
            _validationMessage.postValue("Загрузка была отменена")
            throw e
        } catch (e: Exception) {
            Log.e("PhotoUploadError", "General Error uploading photo: ${e.message}", e)
            _validationMessage.postValue("Неизвестная ошибка: ${e.message}")
            throw e
        } finally {
            _isLoading.value = false
        }
    }

    private fun isInputLangValid(input: String): Boolean {
        val regex = "^[a-zA-Z0-9!@#\$%^&*()_+{}\\[\\]:;<>,.?~\\-=\\s]*\$".toRegex()
        return regex.matches(input)
    }

    fun setAvatar(uri: Uri) {
        _avatar.value = uri
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