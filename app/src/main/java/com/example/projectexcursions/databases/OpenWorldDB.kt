package com.example.projectexcursions.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.models.Excursion

@Database(entities = [Excursion::class], version = 3)
abstract class OpenWorldDB: RoomDatabase() {
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}