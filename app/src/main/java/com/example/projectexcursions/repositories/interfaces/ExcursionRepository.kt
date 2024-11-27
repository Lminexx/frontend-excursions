package com.example.projectexcursions.repositories.interfaces

import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ExcursionResponse
import com.example.projectexcursions.repositories.exlistrepo.ExcursionPagingSource
import retrofit2.Response

interface ExcursionRepository {
    fun getExcursionsPaging(): ExcursionPagingSource

    suspend fun getAllExcursionsFromDB(): List<Excursion>

    suspend fun saveExcursionsToDB(excursions: List<Excursion>)

    suspend fun fetchExcursions(offset: Int, limit: Int): Response<ExcursionResponse>
}