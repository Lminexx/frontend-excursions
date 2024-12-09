package com.example.projectexcursions.repositories.tokenrepo

import android.util.Log
import com.example.projectexcursions.models.Token
import com.auth0.android.jwt.JWT
import com.example.projectexcursions.databases.daos.TokenDao
import com.example.projectexcursions.net.ApiService
import javax.inject.Inject


class TokenRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val tokenDao: TokenDao
): TokenRepository {

    override suspend fun saveToken(token: String) {
        val tokenEntity = Token(token = token)
        tokenDao.insertToken(tokenEntity)
    }

    override suspend fun getToken(): Token? {
        val token = tokenDao.getLatestToken()
        return if (isTokenValid(token.toString()))
            token
        else null
    }

    override fun isTokenValid(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            !jwt.isExpired(0)
        } catch (e: Exception) {
            Log.e("TokenValidator", "Error validating token", e)
            false
        }
    }

    override fun decodeToken(token: String): Map<String, Any>? {
        return try {
            val jwt = JWT(token)
            jwt.claims
        } catch (e: Exception) {
            Log.e("TokenDecoders", "Error decoding token", e)
            null
        }
    }

    override suspend fun getTokens(): List<Token> {
        return tokenDao.getAllTokens()
    }
}