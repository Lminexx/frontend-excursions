package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class UserInformation(
    @ColumnInfo(name = "userId")
    val id: Long,
    val url: String,
    val username: String
) : Parcelable