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

    @Query("select * from excursions order by id desc")
    suspend fun getAllExcursions(): List<ExcursionsList>

    @Query("select * from excursions order by id desc")
    fun getPagingSource(): PagingSource<Int, ExcursionsList>

    @Query("delete from excursions")
    suspend fun clearAll()
}