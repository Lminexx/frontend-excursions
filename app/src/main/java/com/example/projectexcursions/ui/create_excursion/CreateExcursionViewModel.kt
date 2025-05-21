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
import com.example.projectexcursions.models.UserInformation
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.georepo.GeoRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
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

    private val _createExcursion = MutableLiveData<Boolean>()
    val createExcursion: LiveData<Boolean> get() = _createExcursion

    private val _editExcursion = MutableLiveData<Boolean>()
    val editExcursion: LiveData<Boolean> get() = _editExcursion

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

    private val _userPos = MutableLiveData<Point>()
    val userPos: LiveData<Point> get() = _userPos

    private val locationManager = MapKitFactory.getInstance().createLocationManager()

    private val _placeItems = MutableLiveData<List<PlaceItem>>()
    val placeItems: LiveData<List<PlaceItem>> get() = _placeItems

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

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
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        }
    }

    fun editExcursion(context: Context, title: String, description: String, tags:List<String>, topic:String, city: String, id:Long) {
        Log.d("EditingExcursion", "EditingExcursion")
        val excursion = CreatingExcursion(title, description, city, tags, topic)
        val places = placeItems.value ?: emptyList()
        Log.d("places", places.isNotEmpty().toString())
        viewModelScope.launch {
            try {
                _editExcursion.value = false
                val currentExcursion = excursionRepository.fetchExcursion(id).body()
                val respondedExcursion = Excursion(
                    id,
                    title,
                    description,
                    currentExcursion?.user ?: UserInformation(0, "", ""),
                    currentExcursion?.favorite == true,
                    currentExcursion?.rating ?: "0".toFloat(),
                    currentExcursion?.personalRating,
                    tags,
                    topic,
                    currentExcursion?.approvedAt,
                    city
                )

                excursionRepository.editExcursion(id, excursion)
                excursionRepository.saveExcursionToDB(respondedExcursion)
                geoRepository.updatePoints(places, id)

                _message.value = context.getString(R.string.edit_success)

                if (_selectedImages.value?.isNotEmpty() == true) {
                    try {
                        uploadPhotos(context, id)
                    } catch (e: Exception) {
                        Log.e("PhotoUploadError", "Error uploading photos: ${e.message}")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        _message.value = "Error uploading photos: ${e.message}"
                    }
                }
                _wantComeBack.value = true
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                FirebaseCrashlytics.getInstance().recordException(e)
                _createExcursion.value = false
            }
        }
    }

    fun createExcursion(context: Context, title: String, description: String, tags:List<String>, topic:String, city: String) {
        Log.d("CreatingExcursion", "CreatingExcursion")
        val excursion = CreatingExcursion(title, description, city, tags, topic)
        val places = placeItems.value ?: emptyList()
        Log.d("places", places.isNotEmpty().toString())
        viewModelScope.launch {
            try {
                _createExcursion.value = false
                val response = excursionRepository.createExcursion(excursion).body()!!
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
                val id = response.id

                excursionRepository.saveExcursionToDB(respondedExcursion)
                geoRepository.uploadPlacesItems(places, id)

                _message.value = context.getString(R.string.create_success)

                if (_selectedImages.value?.isNotEmpty() == true) {
                    try {
                        uploadPhotos(context, response.id)
                    } catch (e: Exception) {
                        Log.e("PhotoUploadError", "Error uploading photos: ${e.message}")
                        FirebaseCrashlytics.getInstance().recordException(e)
                        _message.value = "Error uploading photos: ${e.message}"
                    }
                }
                _wantComeBack.value = true
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
                Log.e("CreatingExcursionError", e.message!!)
                FirebaseCrashlytics.getInstance().recordException(e)
                _createExcursion.value = false
            }
        }
    }

    private suspend fun uploadPhotos(context: Context, excursionId: Long) {
        try {
            val multipartBodyParts = _selectedImages.value?.mapNotNull { uri ->
                if (uri.scheme == "http" || uri.scheme == "https") {
                    Log.d("PhotoUpload", "Skipping remote URI: $uri")
                    null
                } else {
                    val file = getFileFromUri(context, uri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("files", file.name, requestFile)
                }
            } ?: emptyList()
            val excursionIdRequest =
                excursionId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            println(multipartBodyParts.size)
            excursionRepository.uploadPhotos(multipartBodyParts, excursionIdRequest)
            Log.d("PhotoUpload", "Uploaded photos successfully")
            _selectedImages.postValue(emptyList())
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("photo_exc", e.message.toString())
        }
    }

    fun isExcursionCorrect(context: Context, title: String, description: String, places: List<PlaceItem>, city: String, photos: List<Uri>): Boolean {
        when {
            title.isBlank() -> {
                _message.value = context.getString(R.string.empty_title)
                return false
            }

            description.isBlank() -> {
                _message.value = context.getString(R.string.empty_desc)
                return false
            }

            places.isEmpty() -> {
                _message.value = context.getString(R.string.empty_route)
                return false
            }

            city.isEmpty() -> {
                _message.value = context.getString(R.string.empty_city_name)
                return false
            }

            photos.isEmpty() -> {
                _message.value = context.getString(R.string.empty_photos)
                return false
            }

            else -> return true
        }
    }

    suspend fun getRoute() {
        try {
            val places = placeItems.value ?: return
            if (places.size < 2) {
                _routeLiveData.postValue(emptyList())
            } else if (places.size <= 12) {
                val fullRoute = mutableListOf<Point>()
                withContext(Dispatchers.IO) {
                    for (i in 0 until places.lastIndex) {
                        val a = places[i]
                        val b = places[i + 1]
                        val segment = geoRepository.getRoute(
                            Point(a.lat, a.lon),
                            Point(b.lat, b.lon)
                        )
                        fullRoute.addAll(segment)
                    }
                }
                _routeLiveData.postValue(fullRoute)
            } else {
                _message.value = "Выберите менее 12 мест"
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Route", "Error getting route", e)
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

    fun clickEditExcursion(){
        _editExcursion.value = true
    }

    fun addSelectedImages(images: List<Uri>) {
        _selectedImages.value = _selectedImages.value?.plus(images)
    }

    fun addSelectedImages(image: Uri?) {
        _selectedImages.value = _selectedImages.value?.plus(image) as List<Uri>?
    }

    fun updateSearchResults(results: List<SearchResult>) {
        try {
            _searchResults.value = results
            _isSearchResultsVisible.value = results.isNotEmpty()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.d("Exception", "казявка")
        }
    }

    fun addPlace(placeItem: PlaceItem) {
        val updatedList = _placeItems.value?.toMutableList() ?: mutableListOf()
        Log.d("PlaceItem", placeItem.name ?: "Место X")
        updatedList.add(placeItem)
        _placeItems.value = updatedList
    }

    fun loadPlaces(excursionId: Long) {
        viewModelScope.launch {
            try {
                _placeItems.value=emptyList<PlaceItem>()
                val response = geoRepository.loadPlaces(excursionId).body()!!
                for(place in response){
                    addPlace(place)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("PLacesError", e.message.toString())
            }
        }
    }

    fun deletePlace(placeId: String) {
        try {
            _placeItems.value = _placeItems.value?.filterNot { it.id == placeId }
            Log.d("PLaceItems", "${placeItems.value?.size ?: "null"}")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("DeleteException", e.message.toString())
        }
    }

    fun getId(i:Int): String {
        return geoRepository.getRandomId(i)
    }

    fun clearRouteData() {
        _routeLiveData.value = emptyList()
    }
}