package com.example.projectexcursions.dbs.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectexcursions.dbs.entities.ExcursionEntity

@Dao
interface ExcursionDao {
        
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(excursion: ExcursionEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(excursions: List<ExcursionEntity>)

        @Query("SELECT * FROM excursions")
        suspend fun getAllExcursions(): List<ExcursionEntity>

        @Query("DELETE FROM excursions")
        suspend fun clearAllExcursions()
}