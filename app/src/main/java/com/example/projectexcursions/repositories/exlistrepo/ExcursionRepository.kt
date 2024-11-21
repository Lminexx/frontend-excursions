package com.example.projectexcursions.repositories.exlistrepo

import androidx.paging.PagingSource
import com.example.projectexcursions.databases.excursionsdb.ExcursionDao
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.net.ExcursionResponse
import retrofit2.Response

class ExcursionRepository(
    private val apiService: ApiService,
    private val excursionDao: ExcursionDao
) {
    fun getExcursionsPaging(): PagingSource<Int, Excursion> {
        return ExcursionPagingSource(apiService)
    }

    fun getAllExcursionsFromDB(): List<Excursion> {
        return excursionDao.getAllExcursions()
    }

    suspend fun saveExcursionsToDB(excursions: List<Excursion>) {
        excursionDao.insertAll(excursions)
    }

    suspend fun fetchExcursions(offset: Int, limit: Int): Response<ExcursionResponse> {
        return apiService.getExcursions(offset, limit)
    }
}