package com.example.projectexcursions.net

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class PhotoResponse(
    val id: Long,
    val url: String,
    val excursionId: Long
): Parcelable
