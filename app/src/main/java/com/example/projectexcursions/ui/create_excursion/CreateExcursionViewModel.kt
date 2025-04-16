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
import com.example.projectexcursions.models.PlaceItem
import com.example.projectexcursions.models.SearchResult
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.georepo.GeoRepository
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class CreateExcursionViewModel @Inject constructor(
    private val excursionRepository: ExcursionRepository,
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

    private val _prevPoint = MutableLiveData<Point?>()
    val prevPoint: LiveData<Point?> get() = _prevPoint

    private val _curPoint = MutableLiveData<Point?>()
    val curPoint: LiveData<Point?> get() = _curPoint

    private val _userPos = MutableLiveData<Point>()
    val userPos: LiveData<Point> get() = _userPos

    private val locationManager = MapKitFactory.getInstance().createLocationManager()

    private val _placeItems = MutableLiveData<List<PlaceItem>>()
    val placeItems: LiveData<List<PlaceItem>> get() = _placeItems

    private val _deletingPlaceId = MutableLiveData<String>()
    val deletingPLaceId: LiveData<String> get() = _deletingPlaceId

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
                    response.favorite,
                    response.rating,
                    response.personalRating,
                    response.tags,
                    response.topic,
                    response.approvedAt,
                    response.cityName
                )
                excursionRepository.saveExcursionToDB(respondedExcursion)

                _message.value = context.getString(R.string.create_success)

                if (_selectedImages.value?.isNotEmpty() == true) {
                    try {
                        uploadPhotos(context, response.id)
                    } catch (e: Exception) {
                        Log.e("PhotoUploadError", "Error uploading photos: ${e.message}")
                        _message.value = "Error uploading photos: ${e.message}"
                    }
                }
                _wantComeBack.value = true
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("CreatingExcursionError", e.message!!)
                _createExcursion.value = false
            }
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
        } catch (e: Exception) {
            throw e
        }
    }

    fun isExcursionCorrect(context: Context, title: String, description: String, places: List<PlaceItem>): Boolean {
        when {
            title.isBlank() -> {
                _message.value = context.getString(R.string.empty_title)
                return false
            }

            description.isBlank() -> {
                _message.value = context.getString(R.string.empty_desc)
                return false
            }
            
            places.isNullOrEmpty() -> {
                _message.value = context.getString(R.string.empty_route)
                return true
            }
            else -> return true
        }
    }


    suspend fun getRoute() {
        try {
            val end = curPoint.value
            val start = prevPoint.value
            if (end == null || start == null) {
                Log.d("Point", "Start or end point is null")
                return
            }
            withContext(Dispatchers.IO) {
                val route = geoRepository.getRoute(start, end)
                _routeLiveData.postValue(route)
            }
        } catch (e: Exception) {
            Log.e("Route", "Error getting route", e)
        }
    }

    suspend fun getPhotos(point: Point): List<String> {
        try {
            return geoRepository.getPhotosByLocation(point)
        } catch (e: Exception) {
            Log.e("GetPhotoException", "${e.message}")
            return emptyList()
        }
    }

    fun getUserPosition() {
        Log.d("GetUserPosition", "da!")
        locationManager.requestSingleUpdate(object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                Log.d("getUserLocation", "onLocationUpdated")
                _userPos.value = location.position
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                Log.d("getUserLocation", "onLocationStatusUpdated")
                when (locationStatus) {
                    LocationStatus.NOT_AVAILABLE -> Log.e("Location", "Not available")
                    LocationStatus.AVAILABLE -> Log.d("Location", "Available")
                    LocationStatus.RESET -> Log.d("Location", "Reset")
                }
            }
        })
    }

    fun setPoint(point: Point) {
        if (_curPoint.value == null) {
            _curPoint.value = point
            Log.d("curPoint", "${point.latitude}, ${point.longitude}")
        } else {
            _prevPoint.value = curPoint.value
            _curPoint.value = point
            Log.d("Меняем точки", "true")
            Log.d("curPoint", "${point.latitude}, ${point.longitude}")
            Log.d("prevPoint", "${_prevPoint.value?.latitude}, ${_prevPoint.value?.longitude}")
        }
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
        try {
            _searchResults.value = results
            _isSearchResultsVisible.value = results.isNotEmpty()
        } catch (e: Exception) {
            Log.d("Exception", "казявка")
        }
    }

    fun addPlace(placeItem: PlaceItem) {
        val updatedList = _placeItems.value?.toMutableList() ?: mutableListOf()
        Log.d("PlaceItem", placeItem.name)
        updatedList.add(placeItem)
        _placeItems.value = updatedList
    }

    fun deletePlace(placeId: String) {
        try {
            _deletingPlaceId.value = placeId
            _placeItems.value = _placeItems.value?.filterNot { it.id == placeId }
            Log.d("PLaceItems", "${placeItems.value?.size ?: "null"}")
        } catch (e: Exception) {
            Log.e("DeleteException", e.message.toString())
        }
    }

    fun getId(i: Int): String {
        return geoRepository.getRandomId(i)
    }

    fun resetPoints() {
        _prevPoint.value = null
        _curPoint.value = null
        _routeLiveData.value = emptyList()
    }

    fun clearRouteData() {
        _routeLiveData.value = emptyList()
    }

    fun deletePrevPoint() {
        _prevPoint.value = null
    }
}