package com.example.projectexcursions.dbs

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectexcursions.dbs.daos.ExcursionDao
import com.example.projectexcursions.dbs.entities.ExcursionEntity

@Database(entities = [ExcursionEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun excursionDao(): ExcursionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "excursions_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}