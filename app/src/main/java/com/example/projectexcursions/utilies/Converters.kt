package com.example.projectexcursions.utilies

import androidx.room.TypeConverter
import com.example.projectexcursions.models.UserInformation
import com.example.projectexcursions.net.PhotoResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromPhotoResponse(value: PhotoResponse): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toPhotoResponse(value: String): PhotoResponse {
        return json.decodeFromString(value)
    }
}