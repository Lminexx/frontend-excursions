package com.example.projectexcursions.databases.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectexcursions.models.Excursion

@Dao
interface ExcursionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(excursion: Excursion)

    @Query("SELECT * FROM excursion WHERE id = :id")
    suspend fun getExcursionById(id: Long): Excursion

    @Query("delete from excursion")
    suspend fun clearAll()
}