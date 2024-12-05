package com.example.projectexcursions.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.databases.daos.ExcursionsDao
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionsList

@Database(entities = [ExcursionsList::class, Excursion::class], version = 1)
abstract class OpenWorldDB: RoomDatabase() {
    abstract fun excursionsDao(): ExcursionsDao

    abstract fun excursionDao(): ExcursionDao

    companion object {
        @Volatile
        private var INSTANCE: OpenWorldDB? = null

        fun getDatabase(context: Context): OpenWorldDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OpenWorldDB::class.java,
                    "OpenWorldDB"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}