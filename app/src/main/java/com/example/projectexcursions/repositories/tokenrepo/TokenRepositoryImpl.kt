package com.example.projectexcursions.repositories.tokenrepo

import android.util.Log
import com.auth0.android.jwt.Claim
import com.example.projectexcursions.models.Token
import com.auth0.android.jwt.JWT
import com.example.projectexcursions.databases.daos.TokenDao
import javax.inject.Inject


class TokenRepositoryImpl @Inject constructor(
    private val tokenDao: TokenDao
): TokenRepository {

    private var cachedToken: Token? = null

    override suspend fun saveToken(token: Token) {
        tokenDao.insertToken(token)
        cachedToken = token
    }

    override suspend fun getToken(): Token? {
        val token = tokenDao.getLatestToken()
        cachedToken = token
        Log.d("GetToken", cachedToken?.token ?: "null")
        return cachedToken
    }

    override suspend fun clearToken() {
        tokenDao.clearAll()
        cachedToken = null
    }

    override suspend fun deleteToken(token: String) {
        tokenDao.deleteToken(token)
        cachedToken = null
        Log.d("DeletedCachedToken", cachedToken?.token ?: "null")
    }

    override suspend fun getTokens(): List<Token?> {
        return tokenDao.getAllTokens()
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

    override fun decodeToken(token: String): Map<String, Claim>? {
        return try {
            val jwt = JWT(token)
            jwt.claims
        } catch (e: Exception) {
            Log.e("TokenDecoders", "Error decoding token", e)
            null
        }
    }

    override fun getCachedToken(): Token? {
        return if (cachedToken != null)
            cachedToken
        else
            null
        Log.d("GetCachedToken","$cachedToken")
    }
}