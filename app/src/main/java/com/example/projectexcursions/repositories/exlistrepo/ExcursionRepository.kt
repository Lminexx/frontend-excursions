package com.example.projectexcursions.repositories.exlistrepo

import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ExcursionResponse

interface ExcursionRepository {
    fun getExcursionsPaging(): ExcursionPagingSource

    suspend fun getAllExcursionsFromDB(): List<Excursion>

    suspend fun saveExcursionsToDB(excursions: List<Excursion>)

    suspend fun fetchExcursions(offset: Int, limit: Int): ExcursionResponse
}