package com.example.projectexcursions.repositories

import com.example.projectexcursions.dbs.daos.ExcursionDao
import com.example.projectexcursions.dbs.entities.ExcursionEntity
import com.example.projectexcursions.net.ApiService

class ExcursionRepository(private val apiService: ApiService, private val excursionDao: ExcursionDao) {

    suspend fun getExcursionsFromDb(): List<ExcursionEntity> {
        return excursionDao.getAllExcursions()
    }

    private fun fetchExcursionsFromApi(page: Int, size: Int): List<ExcursionEntity> {
        val response = apiService.getExcursions(page, size)
        return response.body()?.content?.map { excursion ->
            ExcursionEntity(
                id = excursion.id,
                title = excursion.title,
                description = excursion.description
            )
        } ?: emptyList()
    }

    private suspend fun saveExcursionsToDb(excursions: List<ExcursionEntity>) {
        excursionDao.insertAll(excursions)
    }

    suspend fun loadExcursions(page: Int, size: Int): List<ExcursionEntity> {
        val excursions = fetchExcursionsFromApi(page, size)
        saveExcursionsToDb(excursions)
        return excursions
    }
}