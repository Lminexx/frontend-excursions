package com.example.projectexcursions.ui.create_excursion

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    //todo добавить текст из message в resources/string (context как раз для этого)
    fun createExcursion(context: Context, title: String, description: String) {
        when {
            username.value.isNullOrEmpty() -> _message.value = "Username is null or empty"
            description.isEmpty() -> _message.value = "Description is empty"
            title.isEmpty() -> _message.value = "Title is empty"
            else -> {
                val excursion = CreatingExcursion(title, description, username.value!!)
                viewModelScope.launch {
                    try {
                        val response = excursionRepository.createExcursion(excursion)
                        excursionRepository.saveExcursionToDB(response.excursion)
                        _message.value = "Excursion created successfully"
                        _createExcursion.value = true
                    } catch (e: Exception) {
                        _message.value = "Error: ${e.message}"
                        Log.e("CreatingExcursionError: ", e.message!!)
                    }
                }
            }
        }
    }

    private fun getUsername() {
        try {
            val token = tokenRepository.getCachedToken()
            val decodedToken = token?.let { tokenRepository.decodeToken(it.token) }
            val username = decodedToken?.get("username")!!.asString()
            _username.value = username!!
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

    fun excursionCreated() {
        _createExcursion.value = false
    }
}