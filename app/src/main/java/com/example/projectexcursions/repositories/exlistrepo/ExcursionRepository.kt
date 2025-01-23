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

    fun searchExcursionPagingSource(query: String): PagingSource<Int, ExcursionsList>

    fun createdExcursionsPagingSource(): PagingSource<Int, ExcursionsList>

    suspend fun getAllExcursionsFromDB(): List<ExcursionsList>

    suspend fun saveExcursionsToDB(excursionsLists: List<ExcursionsList>)

    suspend fun saveExcursionToDB(excursion: Excursion)

    suspend fun fetchExcursions(offset: Int, limit: Int, isFavorite: Boolean): ExcursionsResponse

    suspend fun fetchExcursion(id: Long): ExcursionResponse

    suspend fun getExcursionFromDB(id: Long): Excursion?

    suspend fun deleteAllExcursionsFromExcursions()

    suspend fun deleteAllExcursionsFromExcursion()

    suspend fun createExcursion(creatingExcursion: CreatingExcursion): ExcursionResponse

    suspend fun searchExcursions(query: String, offset: Int, limit: Int, isFavorite: Boolean): ExcursionsResponse

    suspend fun getCreatedExcursions(offset: Int, limit: Int): ExcursionsResponse
}