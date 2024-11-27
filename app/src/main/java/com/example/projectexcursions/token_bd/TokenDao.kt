package com.example.projectexcursions.token_bd

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TokenDao {
    @Insert
    suspend fun insertToken(token: TokenEntity)

    @Query("SELECT * FROM tokens")
    suspend fun getAllTokens(): List<TokenEntity>

    @Query("DELETE FROM tokens") // Удаление всех токенов (для очистки данных при необходимости)
    suspend fun deleteAllTokens()
}