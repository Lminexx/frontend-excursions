package com.example.projectexcursions.models

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.projectexcursions.serializers.BigDecimalSerializer
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import java.math.BigDecimal

@Parcelize
@Serializable
@Entity(tableName = "excursion")
data class Excursion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val description: String,
    @Embedded
    val user: UserInformation,
    val favorite: Boolean = false,
    @Serializable(with = BigDecimalSerializer::class)
    val rating: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val personalRating: BigDecimal? = null
) : Parcelable
