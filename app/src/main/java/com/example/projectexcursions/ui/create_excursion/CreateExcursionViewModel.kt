package com.example.projectexcursions.ui.create_excursion

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.R
import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
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
class CreateExcursionViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val excursionRepository: ExcursionRepository
) : ViewModel() {

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _createExcursion = MutableLiveData<Boolean>()
    val createExcursion: LiveData<Boolean> get() = _createExcursion

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _selectedImages = MutableLiveData<List<Uri>>(emptyList())
    val selectedImages: LiveData<List<Uri>> get() = _selectedImages

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

    fun createExcursion(context: Context, title: String, description: String) {
        Log.d("CreatingExcursion", "CreatingExcursion")
        val excursion = CreatingExcursion(title, description)
        viewModelScope.launch {
            try {
                _createExcursion.value = false
                val response = excursionRepository.createExcursion(excursion)
                val respondedExcursion = Excursion(
                    response.id,
                    response.title,
                    response.description,
                    response.user,
                    response.favorite
                )
                excursionRepository.saveExcursionToDB(respondedExcursion)

                _message.value = context.getString(R.string.create_success)

                if (_selectedImages.value?.isNotEmpty() == true) {
                    uploadPhotos(context, response.id)
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("CreatingExcursionError", e.message!!)
                _createExcursion.value = false
            }
        }
    }

    fun isExcursionCorrect(context: Context, title: String, description: String): Boolean {
        when {
            title.isBlank() -> {
                _message.value = context.getString(R.string.empty_title)
                return false
            }

            description.isBlank() -> {
                _message.value = context.getString(R.string.empty_desc)
                return false
            }

            else -> return true
        }
    }


    private suspend fun uploadPhotos(context: Context, excursionId: Long) {
        try {
            val multipartBodyParts = _selectedImages.value?.map { uri ->
                val file = getFileFromUri(context, uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files", file.name, requestFile)
            } ?: emptyList()
            val excursionIdRequest =
                excursionId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val response = excursionRepository.uploadPhotos(multipartBodyParts, excursionIdRequest)
            Log.d("PhotoUpload", "Uploaded photos successfully")
            _selectedImages.postValue(emptyList())
            _wantComeBack.value = true
        } catch (e: Exception) {
            Log.e("PhotoUploadError", "Error uploading photos: ${e.message}")
            _message.value = "Error uploading photos: ${e.message}"
        }
    }


    fun clickCreateExcursion() {
        _createExcursion.value = true
    }

    fun addSelectedImages(images: List<Uri>) {
        _selectedImages.value = _selectedImages.value?.plus(images)
    }
}