package com.example.projectexcursions.token_bd

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TokenEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDao
}
