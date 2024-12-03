package com.example.projectexcursions.dbs.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Excursions")
data class ExcursionEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String
)