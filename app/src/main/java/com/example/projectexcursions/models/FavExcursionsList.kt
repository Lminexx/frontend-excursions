package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Entity(tableName = "favorite_excursions")
data class FavExcursionsList(
    @PrimaryKey val id: Long,
    val title: String,
    val userId: Long,
    val description: String
): Parcelable