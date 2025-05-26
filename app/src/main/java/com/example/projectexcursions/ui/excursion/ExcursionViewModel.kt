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
import com.example.projectexcursions.utilies.ApproveExcursionException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import androidx.core.net.toUri

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

    private val _favorite = MutableLiveData(false)
    val favorite: LiveData<Boolean> get() = _favorite

    private val _username = MutableLiveData<String?>()
    val username: LiveData<String?> get() = _username

    private val _photos = MutableLiveData<List<Uri>>()
    val photos: LiveData<List<Uri>> get() = _photos

    private val _places = MutableLiveData<List<PlaceItem>>()
    val places: LiveData<List<PlaceItem>> get() = _places

    private val _route = MutableLiveData<List<Point>>(emptyList())
    val route: LiveData<List<Point>> get() = _route

    private val _approve = MutableLiveData<Boolean>()
    val approve: LiveData<Boolean> get() = _approve

    private val _id = MutableLiveData<Long>()
    val id: LiveData<Long> get() = _id

    private val _isMine = MutableLiveData<Boolean>()
    val isMine: LiveData<Boolean> get() = _isMine

    private val _rating = MutableLiveData<Float>()
    val rating: LiveData<Float> get() = _rating

    private val _myRating = MutableLiveData<Float>()
    val myRating: LiveData<Float> get() = _myRating

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
                Log.e("LoadExcursion", e.message ?: "null")
                _excursion.value = null
            }
        }
    }

    fun loadPhotos(excursionId: Long) {
        viewModelScope.launch {
            try {
                val response = excRepository.loadPhotos(excursionId)
                val photoUris = response.body()!!.map { it.url.toUri() }
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
                _places.postValue(response)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("PLacesError", e.message.toString())
            }
        }
    }

    suspend fun getRoute() {
        try {
            val places = places.value ?: return
            if (places.size < 2) {
                _route.postValue(emptyList())
            } else {
                val fullRoute = mutableListOf<Point>()
                withContext(Dispatchers.IO) {
                    for (i in 0 until places.lastIndex) {
                        val a = places[i]
                        val b = places[i + 1]
                        if (a.lat != b.lat && a.lon != b.lon) {
                            val segment = geoRepository.getRoute(
                                Point(a.lat, a.lon),
                                Point(b.lat, b.lon)
                            )
                            fullRoute.addAll(segment)
                        }
                    }
                }
                _route.postValue(fullRoute)
            }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("Route", "Error getting route", e)
        }
    }

    private fun getUsername() {
        val token = tokenRepository.getCachedToken()
        val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
        val name = decodedToken?.get("username")!!.asString()
        _username.value = name
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

    fun isMine() {
        _excursion.value?.let { currentExcursion ->
            _isMine.value = currentExcursion.user.username == _username.value
        }
    }

    suspend fun updateRating(rating: Float) {
        val excursionId = _excursion.value?.id ?: 0L
        try {
            val response = excRepository.uploadRating(excursionId, rating).body()!!
            _rating.postValue(response.ratingAVG)
            _myRating.postValue(rating)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("UpdateRating", "Error updating rating for excursion $excursionId", e)
            _excursion.value?.rating ?: 0.0f
        }
    }

    fun deleteExcursion() {
        try {
            viewModelScope.launch {
                Log.d("DeleteEx", "DeleteExcursion")
                excursion.value?.let { excRepository.deleteExcursion(it.id) }
                _wantComeBack.value = true
            }
        } catch (http: HttpException) {
            FirebaseCrashlytics.getInstance().recordException(http)
        } catch (io: IOException) {
            FirebaseCrashlytics.getInstance().recordException(io)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    suspend fun excursionPended(id: Long) {
        excRepository.changeExcursionStatus(id, "PENDING")
    }

    suspend fun excursionRejected(id: Long) {
        excRepository.changeExcursionStatus(id, "REJECTED")
    }

    suspend fun excursionApproved() {
        val id = excursion.value?.id ?: throw ApproveExcursionException()
        excRepository.changeExcursionStatus(id, "APPROVED")
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = tokenRepository.getToken()
        return token != null
    }
}