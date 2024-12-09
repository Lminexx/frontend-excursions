package com.example.projectexcursions.repositories.tokenrepo

import com.example.projectexcursions.models.Token

interface TokenRepository {
    suspend fun saveToken(token: String)

    suspend fun getToken(): String?

    fun isTokenValid(token: String): Boolean

    fun decodeToken(token: String): Map<String, Any>?

    suspend fun getTokens(): List<Token>
}