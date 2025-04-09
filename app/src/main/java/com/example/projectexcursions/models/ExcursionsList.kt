package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.projectexcursions.serializers.BigDecimalJson
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize


@Parcelize
@Serializable
@Entity(tableName = "excursions")
data class ExcursionsList(
    @PrimaryKey val id: Long,
    val title: String,
    val userId: Long,
    val description: String,
    val favorite: Boolean = false,
    val rating: BigDecimalJson,
    val personalRating: BigDecimalJson? = null
): Parcelable
