package com.example.projectexcursions.repositories.exlistrepo

import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ExcursionResponse
import com.example.projectexcursions.net.ExcursionsResponse

interface ExcursionRepository {
    fun excursionPagingSource(): PagingSource<Int, ExcursionsList>

    suspend fun getAllExcursionsFromDB(): List<ExcursionsList>

    suspend fun saveExcursionsToDB(excursionsLists: List<ExcursionsList>)

    suspend fun saveExcursionToDB(excursion: Excursion)

    suspend fun fetchExcursions(offset: Int, limit: Int): ExcursionsResponse

    suspend fun fetchExcursion(id: Long): ExcursionResponse

    suspend fun getExcursionFromDB(id: Long): Excursion?

    suspend fun deleteAllExcursionsFromExcursions()

    suspend fun deleteAllExcursionsFromExcursion()

    suspend fun createExcursion(creatingExcursion: CreatingExcursion): ExcursionResponse

    suspend fun addFavorite(token:String, id:Long)

    suspend fun deleteFavorite(token:String, id:Long)
}