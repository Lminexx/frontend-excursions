package com.example.projectexcursions.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody

@Serializable
@Entity(tableName = "tokens")
data class Token (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val token: String
)