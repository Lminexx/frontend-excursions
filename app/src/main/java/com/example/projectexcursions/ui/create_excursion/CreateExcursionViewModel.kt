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
import com.example.projectexcursions.models.SearchResult
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.georepo.GeoRepository
import com.example.projectexcursions.repositories.pointrepo.PointRepository
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateExcursionViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val excursionRepository: ExcursionRepository,
    private val pointRepository: PointRepository,
    private val geoRepository: GeoRepository
): ViewModel() {

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

    private val _isSearchResultsVisible = MutableLiveData(false)
    val isSearchResultsVisible: LiveData<Boolean> get() = _isSearchResultsVisible

    private val _searchResults = MutableLiveData<List<SearchResult>>(emptyList())
    val searchResults: LiveData<List<SearchResult>> get() = _searchResults

    private val _routeLiveData = MutableLiveData<List<Point>>(emptyList())
    val routeLiveData: LiveData<List<Point>> get() = _routeLiveData

    private val _prevPoint = MutableLiveData<Point>()
    val prevPoint: LiveData<Point> get() = _prevPoint

    private val _nextPoint = MutableLiveData<Point>()
    val nextPoint: LiveData<Point> get() = _nextPoint



    private fun getFileFromUri(context: Context, uri: Uri): File {
        val fileName = "upload_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }

    fun createExcursion(context: Context, title: String, description: String) {
        Log.d("CreatingExcursion", "CreatingExcursion")
        val excursion = CreatingExcursion(title, description)
        viewModelScope.launch {
            try {
                val response = excursionRepository.createExcursion(excursion)
                val respondedExcursion = Excursion(
                    response.id,
                    response.title,
                    response.userId,
                    response.description,
                    response.username,
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

    private fun uploadPhotos(context: Context, excursionId: Long) {
        viewModelScope.launch {
            val jobs = mutableListOf<Job>()

            _selectedImages.value?.forEach { uri ->
                val job = launch {
                    try {
                        val file = getFileFromUri(context, uri)

                        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                        val multipartBody =
                            MultipartBody.Part.createFormData("file", file.name, requestFile)
                        val fileName = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
                        val excursionIdRequest =
                            excursionId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                        val response = excursionRepository.uploadPhoto(fileName, multipartBody, excursionIdRequest)
                        Log.d("PhotoUpload", "Uploaded photo: ${response.url}")
                    } catch (e: Exception) {
                        Log.e("PhotoUploadError", "Error uploading photo: ${e.message}")
                    }
                }
                jobs.add(job)
            }
            jobs.forEach { it.join() }
            _selectedImages.postValue(emptyList())
            _wantComeBack.value = true
        }
    }

    suspend fun getRoute() {
        try {
            val end = pointRepository.getCachedEnd()
            val start = pointRepository.getCachedStart()
            if (end == null || start == null) {
                Log.d("Point", "Start or end point is null")
                return
            }
            val route = geoRepository.getRoute(start, end)
            _routeLiveData.value = route
        } catch (e: Exception) {
            Log.e("Route", "Error getting route", e)
        }
    }

    fun setPrevPoint(point: Point) {
        _prevPoint.value = point
    }

    fun setNextPoint(point: Point) {
        _nextPoint.value = point
    }

    fun toggleSearchResultsVisibility() {
        _isSearchResultsVisible.value = _isSearchResultsVisible.value?.not()
    }

    fun deleteSearchResults() {
        _searchResults.value = emptyList()
        _isSearchResultsVisible.value = false
    }

    fun hideSearchResults() {
        _isSearchResultsVisible.value = false
    }

    fun clickCreateExcursion() {
        _createExcursion.value = true
    }

    fun addSelectedImages(images: List<Uri>) {
        _selectedImages.value = _selectedImages.value?.plus(images)
    }

    fun updateSearchResults(results: List<SearchResult>) {
        _searchResults.value = results
        _isSearchResultsVisible.value = results.isNotEmpty()
    }
}