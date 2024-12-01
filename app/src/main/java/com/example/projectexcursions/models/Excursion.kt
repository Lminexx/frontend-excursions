package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
@Entity(tableName = "excursion")
data class Excursion(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val userId: Long
): Parcelable
