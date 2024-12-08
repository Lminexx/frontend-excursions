package com.example.projectexcursions.token_bd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExampleViewModel(private val repository: TokenRepository) : ViewModel() {

    fun saveToken(token: String) {
        viewModelScope.launch {
            repository.saveToken(token)
        }
    }

    suspend fun loadToken(): String? {
        return repository.getToken()
    }

    fun checkIfTokenIsValid(token: String): Boolean {
        return repository.isTokenValid(token)
    }

    fun decodeGivenToken(token: String): Map<String, Any>? {
        return repository.decodeToken(token)
    }

    suspend fun loadAllTokens(): List<TokenEntity> {
        return repository.getTokens()
    }
}
