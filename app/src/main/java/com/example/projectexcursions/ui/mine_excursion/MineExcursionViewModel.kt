package com.example.projectexcursions.ui.mine_excursion

import android.net.Uri
import android.text.BoringLayout
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MineExcursionViewModel @Inject constructor(
    private val apiService: ApiService,
    private val repository: ExcursionRepository,
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

    //todo надо сделать везде такое для вывода сообщений об ошибках
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _photos = MutableLiveData<List<Uri>>()
    val photos: LiveData<List<Uri>> get() = _photos

    init {
        if (tokenRepository.getCachedToken() != null)
            getUsername()
    }

    fun loadExcursion(excursionId: Long) {
        viewModelScope.launch {
            try {
                if (repository.getExcursionFromDB(excursionId) != null) {
                    val excursionFromDB = repository.getExcursionFromDB(excursionId)
                    _excursion.value = excursionFromDB
                    Log.d("ExcursionInDB", "ExcExists")
                } else {
                    val response = repository.fetchExcursion(id = excursionId)
                    Log.d("ExcContent", "${response.id}, \n${response.title}, \n${response.userId}, " +
                            "\n${response.description}, \n${response.username}, \n${response.favorite}")
                    val excursion = Excursion(
                        response.id,
                        response.title,
                        response.userId,
                        response.description,
                        response.username,
                        response.favorite
                    )
                    repository.saveExcursionToDB(excursion)
                    _excursion.value = excursion
                    Log.d("ExcursionIsnInDB", "FetchExcursion")
                }
            } catch (e: Exception) {
                Log.e("LoadExcursion", e.message!!)
                _excursion.value = null
            }
        }
    }

    fun loadPhotos(excursionId: Long) {
        viewModelScope.launch {
            try {
                val response = repository.loadPhotos(excursionId)
                if (response.isNotEmpty()) {
                    val photoUris = response.map { Uri.parse(it.url) }
                    _photos.value = photoUris
                }
            } catch (e: Exception) {
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

    fun deleteExcursion(){
        try {
            viewModelScope.launch {
                Log.d("DeleteEx", "DeleteExcursion")
                excursion.value?.let { repository.deleteExcursion(it.id) }
                _wantComeBack.value = true
            }
        } catch (http: HttpException) {
            _message.value = http.message
        } catch (io: IOException) {
            _message.value = io.message
        } catch (e: Exception) {
            _message.value = e.message
        }
    }

    fun cameBack() {
        _wantComeBack.value = false
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
                    repository.deleteFavorite(currentExcursion.id)
                } else {
                    Log.d("FavoriteExcursion", "Adding to favorites")
                    repository.addFavorite(currentExcursion.id)
                }
            }
        }
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = tokenRepository.getToken()
        return token != null
    }
}