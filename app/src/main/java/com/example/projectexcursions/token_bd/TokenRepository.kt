package com.example.projectexcursions.token_bd

import android.content.Context
import com.auth0.android.jwt.JWT

class TokenRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val tokenDao = db.tokenDao()

    suspend fun saveToken(token: String) {
        val tokenEntity = TokenEntity(token = token)
        tokenDao.insertToken(tokenEntity)
    }

    suspend fun getToken(): String? {
        val tokens = tokenDao.getAllTokens()
        return if (tokens.isNotEmpty()) tokens[0].token else null
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val jwt = JWT(token)
            !jwt.isExpired(0)
        } catch (e: Exception) {
            false
        }
    }

    fun decodeToken(token: String): Map<String, Any>? {
        return try {
            val jwt = JWT(token)
            jwt.claims
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTokens(): List<TokenEntity> {
        return tokenDao.getAllTokens()
    }
}
