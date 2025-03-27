package com.example.projectexcursions.repositories.tokenrepo

import android.net.Uri
import com.auth0.android.jwt.Claim
import com.example.projectexcursions.models.Token
import com.example.projectexcursions.net.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface TokenRepository {

    suspend fun saveToken(token: Token)

    suspend fun getToken(): Token?

    suspend fun getTokens(): List<Token?>

    suspend fun clearToken()

    fun isTokenValid(token: String): Boolean

    fun decodeToken(token: String): Map<String, Claim>?

    fun getCachedToken(): Token?

    suspend fun deleteToken(token: String)

    suspend fun validateToken(apiService: ApiService)

}