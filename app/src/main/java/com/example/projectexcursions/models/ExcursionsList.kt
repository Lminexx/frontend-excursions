package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectexcursions.ui.utilies.Converters
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
@Entity(tableName = "excursions")
@TypeConverters(Converters::class)
data class ExcursionsList(
    @PrimaryKey val id: Long,
    val title: String,
    val userId: Long,
    val user: UserInformation,
    val username: String? = null,
    val description: String,
    val favorite: Boolean = false,
    val rating: Float,
    val personalRating: Float? = null,
    val tags: List<String>,
    val topic: String,
    val approvedAt: String,
    val cityName: String,
    val photoId: Long,
    val photoUrl: String
): Parcelable