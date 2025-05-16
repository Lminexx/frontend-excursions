package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectexcursions.net.PhotoResponse
import com.example.projectexcursions.utilies.Converters
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.InternalSerializationApi

@Parcelize
@Serializable
@Entity(tableName = "excursions")
@TypeConverters(Converters::class)
@OptIn(InternalSerializationApi::class)
data class ExcursionsList(
    @PrimaryKey val id: Long,
    val title: String,
    val description: String,
    val userId: Long,
    val favorite: Boolean = false,
    val rating: Float,
    val personalRating: Float?=null,
    val tags: List<String>,
    val topic: String,
    val approvedAt: String?=null,
    val cityName: String,
    val photoUrl: PhotoResponse,
    val user: UserInformation
): Parcelable {
    val url: String get() = photoUrl.url
    val photoId: Long get() = photoUrl.id
    val userName: String get() = user.username
    val userUrl: String get() = user.url
}
