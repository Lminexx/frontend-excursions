package com.example.projectexcursions.ui.excursion

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
import com.example.projectexcursions.ui.utilies.ApproveExcursionException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExcursionViewModel @Inject constructor(
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

    private val _deleteExcursion = MutableLiveData<Boolean>()
    val deleteExcursion: LiveData<Boolean> get() = _deleteExcursion

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _photos = MutableLiveData<List<Uri>>()
    val photos: LiveData<List<Uri>> get() = _photos

    private val _places = MutableLiveData<List<PlaceItem>>()
    val places: LiveData<List<PlaceItem>> get() = _places

    private val _routeLiveData = MutableLiveData<List<Point>>(emptyList())
    val routeLiveData: LiveData<List<Point>> get() = _routeLiveData

    private val _prevPoint = MutableLiveData<Point?>()
    val prevPoint: LiveData<Point?> get() = _prevPoint

    private val _curPoint = MutableLiveData<Point?>()
    val curPoint: LiveData<Point?> get() = _curPoint

    private val _disapproving = MutableLiveData<Boolean>()
    val disapproving: LiveData<Boolean> get() = _disapproving

    private val _approve = MutableLiveData<Boolean>()
    val approve: LiveData<Boolean> get() = _approve

    private val _id = MutableLiveData<Long>()
    val id: LiveData<Long> get() = _id

    init {
        if (tokenRepository.getCachedToken() != null)
            getUsername()
    }

    fun loadExcursion(excursionId: Long) {
        viewModelScope.launch {
            try {
                val response = excRepository.fetchExcursion(id = excursionId).body()!!
                val excursion = Excursion(
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
                excRepository.saveExcursionToDB(excursion)
                _excursion.value = excursion
                Log.d("ExcursionIsnInDB", "FetchExcursion")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("LoadExcursion", e.message!!)
                _excursion.value = null
            }
        }
    }

    fun loadPhotos(excursionId: Long) {
        viewModelScope.launch {
            try {
                val response = excRepository.loadPhotos(excursionId)
                val photoUris = response.body()!!.map { Uri.parse(it.url) }
                _photos.value = photoUris
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("LoadPhotos", e.message ?: "Unknown error")
            }
        }
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
            val route = geoRepository.getRoute(start, end)
            _routeLiveData.postValue(route)
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

    private fun getUsername() {
        val token = tokenRepository.getCachedToken()
        val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
        val name = decodedToken?.get("username")!!.asString()
        _username.value = name!!
    }

    fun disapprove() {
        _disapproving.value = true
    }

    fun fav() {
        _favorite.value = true
    }

    fun notFav() {
        _favorite.value = false
    }

    fun cameBack() {
        _wantComeBack.value = false
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


    suspend fun updateRating(rating: Float): Float {
        val excursionId = _excursion.value?.id ?: return 0.0f
        return try {
            val response = excRepository.uploadRating(excursionId, rating).body()!!
            _excursion.postValue(
                _excursion.value?.copy(
                    rating = response.ratingAVG,
                    personalRating = rating
                )
            )
            response.ratingAVG
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("UpdateRating", "Error updating rating for excursion $excursionId", e)
            _excursion.value?.rating ?: 0.0f
        }
    }

    suspend fun excursionPended(id: Long) {
        excRepository.changeExcursionStatus(id, "PENDING")
        _disapproving.postValue(false)
    }

    suspend fun excursionRejected(id: Long) {
        excRepository.changeExcursionStatus(id, "REJECTED")
        _disapproving.postValue(false)    }

    suspend fun excursionApproved() {
        val id = excursion.value?.id ?: throw ApproveExcursionException()
        excRepository.changeExcursionStatus(id, "APPROVED")
        _disapproving.postValue(false)
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = tokenRepository.getToken()
        return token != null
    }
}