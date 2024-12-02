package com.example.projectexcursions.repositories.exlistrepo

import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.net.ExcursionResponse

class ExcursionRepositoryImpl(
    private val apiService: ApiService,
    private val excursionDao: ExcursionDao
) : ExcursionRepository {
    override fun getExcursionsPaging() = ExcursionPagingSource(apiService)

    override suspend fun getAllExcursionsFromDB() = excursionDao.getAllExcursions()

    override suspend fun saveExcursionsToDB(excursions: List<Excursion>) {
        excursionDao.insertAll(excursions)
    }

    override suspend fun fetchExcursions(offset: Int, limit: Int): ExcursionResponse {
        return apiService.getExcursions(offset, limit)
    }

    override suspend fun deleteAllExcursions() = excursionDao.clearAll()
}