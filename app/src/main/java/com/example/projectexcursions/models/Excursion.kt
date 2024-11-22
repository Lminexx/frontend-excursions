package com.example.projectexcursions.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "excursion")
data class Excursion(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String
)