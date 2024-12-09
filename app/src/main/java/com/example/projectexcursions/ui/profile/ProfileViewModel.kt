package com.example.projectexcursions.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.InvalidTokenException
import com.example.projectexcursions.UsernameNotFoundException
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: TokenRepository
    ): ViewModel() {

    private val _username = MutableLiveData<String?>()
    val username: LiveData<String?> get() = _username

    private val _wantCreate = MutableLiveData<Boolean>()
    val wantCreate: LiveData<Boolean> get() = _wantCreate

    init {
        loadUser()
    }
    private fun loadUser() {
        viewModelScope.launch {
            val token = repository.getToken().toString()
            if (repository.isTokenValid(token)) {
                val decodedToken = repository.decodeToken(token)
                val username = decodedToken?.get("username") as? String
                if (username != null)
                    _username.value = username
                else
                    throw UsernameNotFoundException("Username not found in token")
            }
            else
                throw InvalidTokenException("Token is invalid")
        }
    }

    fun clickCreateExcursion() {
        _wantCreate.value = true
    }

    fun isCreating() {
        _wantCreate.value = false
    }
}