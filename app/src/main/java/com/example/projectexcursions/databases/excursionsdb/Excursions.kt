package com.example.projectexcursions.databases.excursionsdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "excursions")
data class Excursions(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String
)