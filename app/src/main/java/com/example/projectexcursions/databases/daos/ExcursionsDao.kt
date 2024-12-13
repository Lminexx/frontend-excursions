package com.example.projectexcursions.databases.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projectexcursions.models.ExcursionsList

@Dao
interface ExcursionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(excursionsLists: List<ExcursionsList>)
    //todo убрать эти временные костыли
    @Query("select * from excursions")
    suspend fun getAllExcursions(): List<ExcursionsList>

    @Query("select * from excursions")
    fun getPagingSource(): PagingSource<Int, ExcursionsList>

    @Query("delete from excursions")
    suspend fun clearAll()
}