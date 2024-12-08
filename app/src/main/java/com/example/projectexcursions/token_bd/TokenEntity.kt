package com.example.projectexcursions.token_bd

import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "token_table")
data class TokenEntity(
    @PrimaryKey val id: Int = 0,
    val token: String
)
