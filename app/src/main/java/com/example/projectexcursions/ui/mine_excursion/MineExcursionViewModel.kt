package com.example.projectexcursions.ui.mine_excursion

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.PlaceItem
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.georepo.GeoRepository
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MineExcursionViewModel @Inject constructor(
    private val geoRepository: GeoRepository,
    private val excRepository: ExcursionRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _excursion = MutableLiveData<Excursion?>()
    val excursion: LiveData<Excursion?> = _excursion

    private val _favorite = MutableLiveData<Boolean>()
    val favorite: LiveData<Boolean> get() = _favorite

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _photos = MutableLiveData<List<Uri>>(emptyList())
    val photos: LiveData<List<Uri>> get() = _photos

    private val _places = MutableLiveData<List<PlaceItem>>()
    val places: LiveData<List<PlaceItem>> get() = _places

    private val _routeLiveData = MutableLiveData<List<Point>>(emptyList())
    val routeLiveData: LiveData<List<Point>> get() = _routeLiveData

    private val _prevPoint = MutableLiveData<Point?>()
    val prevPoint: LiveData<Point?> get() = _prevPoint

    private val _curPoint = MutableLiveData<Point?>()
    val curPoint: LiveData<Point?> get() = _curPoint

    init {
        if (tokenRepository.getCachedToken() != null)
            getUsername()
    }

    fun loadExcursion(excursionId: Long) {
        viewModelScope.launch {
            try {
                if (excRepository.getExcursionFromDB(excursionId) != null) {
                    val excursionFromDB = excRepository.getExcursionFromDB(excursionId)
                    _excursion.value = excursionFromDB
                    Log.d("ExcursionInDB", "ExcExists")
                } else {
                    val response = excRepository.fetchExcursion(id = excursionId).body()!!
                    Log.d("ExcContent", "${response.id}, \n${response.title}, " +
                            "\n${response.description}, \n${response.user}, \n${response.favorite}")
                    val excursion = Excursion(
                        response.id,
                        response.title,
                        response.description,
                        response.user,
                        response.favorite,
                        response.rating,
                        response.personalRating
                    )
                    excRepository.saveExcursionToDB(excursion)
                    _excursion.value = excursion
                    Log.d("ExcursionIsnInDB", "FetchExcursion")
                }
            } catch (e: Exception) {
                Log.e("LoadExcursion", e.message!!)
                FirebaseCrashlytics.getInstance().recordException(e)
                _excursion.value = null
            }
        }
    }

    fun loadPhotos(excursionId: Long) {
        viewModelScope.launch {
            try {
                val response = excRepository.loadPhotos(excursionId).body()!!
                if (response.isNotEmpty()) {
                    val photoUris = response.map { Uri.parse(it.url) }
                    _photos.value = photoUris
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("LoadPhotos", e.message ?: "Unknown error")
            }
        }
    }

    private fun getUsername() {
            val token = tokenRepository.getCachedToken()
            val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
            val name = decodedToken?.get("username")!!.asString()
            _username.value = name!!
    }

    fun loadPlaces(excursionId: Long) {
        viewModelScope.launch {
            try {
                val response = geoRepository.loadPlaces(excursionId).body()!!
                _places.value = response
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("PLacesError", e.message.toString())
            }
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
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Route", "Error getting route", e)
        }
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

    fun deleteExcursion(){
        try {
            viewModelScope.launch {
                Log.d("DeleteEx", "DeleteExcursion")
                excursion.value?.let { excRepository.deleteExcursion(it.id) }
                _wantComeBack.value = true
            }
        } catch (http: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(http)
            _message.value = http.message
        } catch (io: IOException) {
            FirebaseCrashlytics.getInstance().recordException(io)
            _message.value = io.message
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            _message.value = e.message
        }
    }

    fun fav() {
        _favorite.value = true
    }

    fun notFav() {
        _favorite.value = false
    }

    fun clickFavorite() {
        _excursion.value?.let { currentExcursion ->
            val isCurrentlyFavorite = currentExcursion.favorite
            val updatedExcursion = currentExcursion.copy(favorite = !isCurrentlyFavorite)

            _excursion.value = updatedExcursion

            viewModelScope.launch {
                if (isCurrentlyFavorite) {
                    Log.d("FavoriteExcursion", "Removing from favorites")
                    excRepository.deleteFavorite(currentExcursion.id)
                } else {
                    Log.d("FavoriteExcursion", "Adding to favorites")
                    excRepository.addFavorite(currentExcursion.id)
                }
            }
        }
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = tokenRepository.getToken()
        return token != null
    }
}