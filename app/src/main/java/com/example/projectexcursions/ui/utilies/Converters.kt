package com.example.projectexcursions.ui.utilies

import androidx.room.TypeConverter
import com.example.projectexcursions.models.UserInformation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
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
    fun fromUserInformation(user: UserInformation?): String? {
        return Gson().toJson(user)
    }

    @TypeConverter
    fun toUserInformation(data: String?): UserInformation? {
        return data?.let {
            Gson().fromJson(it, UserInformation::class.java)
        }
    }
}