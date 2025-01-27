package com.example.projectexcursions.ui.excursion

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
import javax.inject.Inject

@HiltViewModel
class ExcursionViewModel @Inject constructor(
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

    private val _deleteExcursion = MutableLiveData<Boolean>()
    val deleteExcursion:LiveData<Boolean> get() = _deleteExcursion

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    init {
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

    private fun getUsername() {
            val token = tokenRepository.getCachedToken()
            val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
            val name = decodedToken?.get("username")!!.asString()
            _username.value = name!!
    }

    fun addFavorite() {
        viewModelScope.launch {
            Log.d("FavoriteExcursion", "AddFavorite")
            excursion.value?.let { repository.addFavorite(it.id) }
        }
    }

    fun deleteFavorite() {
        viewModelScope.launch {
            Log.d("FavoriteExcursion", "DeleteFavorite")
            excursion.value?.let { repository.deleteFavorite(it.id) }
        }
    }

    fun deleteExcursion(){
        viewModelScope.launch {
            Log.d("DeleteEx", "DeleteExcursion")
            excursion.value?.let { repository.deleteExcursion(it.id) }
        }
    }

    fun clickComeback() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }

    fun clickFavorite() {
        _favorite.value = true
    }

    fun clickNotFavorite() {
        _favorite.value = false
    }

    suspend fun checkAuthStatus(): Boolean {
        val token = tokenRepository.getToken()
        return token != null
    }

}
