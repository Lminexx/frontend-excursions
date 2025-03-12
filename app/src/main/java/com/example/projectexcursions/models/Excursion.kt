package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
@Entity(tableName = "excursion")
data class Excursion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    @Embedded
    val user: UserInformation,
    val favorite: Boolean = false
) : Parcelable
