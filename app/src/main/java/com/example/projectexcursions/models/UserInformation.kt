package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@OptIn(InternalSerializationApi::class)
data class UserInformation(
    @ColumnInfo(name = "userId")
    val id: Long,
    val url: String,
    val username: String
): Parcelable