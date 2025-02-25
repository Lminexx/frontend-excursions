package com.example.projectexcursions.databases.daos

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectexcursions.models.Token

@Dao
interface TokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: Token)

    @Query("SELECT * FROM tokens WHERE id = :id")
    suspend fun getTokenById(id: Long): Token?

    @Query("SELECT * FROM tokens ORDER BY id DESC LIMIT 1")
    suspend fun getLatestToken(): Token?

    @Query("select * from tokens")
    suspend fun getAllTokens(): List<Token?>

    @Query("delete from tokens")
    suspend fun clearAll()

    @Query("delete from tokens where token = :token")
    suspend fun deleteToken(token: String)

    @Query("update tokens set avatarUri = :uri  where token = :token")
    suspend fun updateAvatar(token: String, uri: String)

    @Query("select avatarUri from tokens where token = :token")
    suspend fun getAvatar(token: String): String
}