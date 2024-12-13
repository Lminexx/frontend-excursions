package com.example.projectexcursions.ui.create_excursion

import android.content.Context
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
import javax.inject.Inject

@HiltViewModel
class CreateExcursionViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val excursionRepository: ExcursionRepository
): ViewModel() {

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _createExcursion = MutableLiveData<Boolean>()
    val createExcursion: LiveData<Boolean> get() = _createExcursion

    private val _wantCreateExc = MutableLiveData<Boolean>()
    val wantCreateExc: LiveData<Boolean> get() = _wantCreateExc

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message
    init {
        getUsername()
    }

    fun createExcursion(context: Context, title: String, description: String) {
        when {
            description.isBlank() -> _message.value = context.getString(R.string.empty_desc)
            title.isBlank() -> _message.value = context.getString(R.string.empty_title)
            else -> {
                Log.d("CreatingExcursion", "CreatingExcursion")
                val token = tokenRepository.getCachedToken()!!.token
                val excursion = CreatingExcursion(title, description, username.value!!)
                viewModelScope.launch {
                    try {
                        val response = excursionRepository.createExcursion(token, excursion)
                        val respondedExcursion = Excursion(response.id, response.title, response.description, response.username)
                        excursionRepository.saveExcursionToDB(respondedExcursion)
                        _message.value = context.getString(R.string.create_success)
                    } catch (e: Exception) {
                        _message.value = "Error: ${e.message}"
                        Log.e("CreatingExcursionError: ", e.message!!)
                        _createExcursion.value = false
                    }
                }
            }
        }
    }

    private fun getUsername() {
        try {
            val token = tokenRepository.getCachedToken()
            val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
            val name = decodedToken?.get("username")!!.asString()
            _username.value = name!!
            Log.d("Username", "${_username.value}")
        } catch (e: Exception) {
            _message.value = "Username error:\n${e.message}"
            Log.e("GettingUsernameInCreatingExcursion", e.message!!)
        }
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }

    fun clickCreateExcursion() {
        _createExcursion.value = true
    }
}