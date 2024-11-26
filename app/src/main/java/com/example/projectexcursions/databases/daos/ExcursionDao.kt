package com.example.projectexcursions.databases.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectexcursions.models.Excursion

@Dao
interface ExcursionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(excursions: List<Excursion>)

    @Query("select * from excursion")
    suspend fun getAllExcursions(): List<Excursion>
}