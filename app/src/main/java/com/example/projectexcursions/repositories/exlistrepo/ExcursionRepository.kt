package com.example.projectexcursions.repositories.exlistrepo

import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ExcursionResponse
import com.example.projectexcursions.net.ExcursionsResponse
import com.example.projectexcursions.paging_sources.ExcursionPagingSource
import com.example.projectexcursions.paging_sources.SearchExcursionPagingSource

interface ExcursionRepository {
    fun excursionPagingSource(isMine: Boolean): ExcursionPagingSource

    fun searchExcursionPagingSource(query: String, isMine: Boolean): SearchExcursionPagingSource

    suspend fun getAllExcursionsFromDB(): List<ExcursionsList>

    suspend fun saveExcursionsToDB(excursionsLists: List<ExcursionsList>)

    suspend fun saveExcursionToDB(excursion: Excursion)

    suspend fun fetchExcursions(offset: Int, limit: Int, isFavorite: Boolean, isMine: Boolean): ExcursionsResponse

    suspend fun fetchExcursion(id: Long): ExcursionResponse

    suspend fun getExcursionFromDB(id: Long): Excursion?

    suspend fun deleteAllExcursionsFromExcursions()

    suspend fun deleteAllExcursionsFromExcursion()

    suspend fun createExcursion(creatingExcursion: CreatingExcursion): ExcursionResponse

    suspend fun searchExcursions(query: String, offset: Int, limit: Int, isFavorite: Boolean, isMine: Boolean): ExcursionsResponse
}