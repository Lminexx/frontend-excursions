package com.example.projectexcursions.repositories.exlistrepo

import android.util.Log
import androidx.paging.PagingSource
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.databases.daos.ExcursionsDao
import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.net.ExcursionResponse
import com.example.projectexcursions.net.ExcursionsResponse
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import javax.inject.Inject

class ExcursionRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val excursionsDao: ExcursionsDao,
    private val excursionDao: ExcursionDao,
    private val tokenRepository: TokenRepository
) : ExcursionRepository {

    override fun excursionPagingSource() = ExcursionPagingSource(apiService)

    override fun searchExcursionPagingSource(excursionTitle: String) = SearchExcursionPagingSource(apiService, excursionTitle)

    override suspend fun getAllExcursionsFromDB() = excursionsDao.getAllExcursions()

    override suspend fun saveExcursionsToDB(excursionsLists: List<ExcursionsList>) {
        Log.d("InsertExs", "InsertExcursions")
        excursionsDao.insertAll(excursionsLists)
    }

    override suspend fun saveExcursionToDB(excursion: Excursion) {
        Log.d("InsertEx", "InsertExcursion")
        excursionDao.insert(excursion)
    }

    override suspend fun fetchExcursions(offset: Int, limit: Int): ExcursionsResponse {
        Log.d("FetchingExs", "FetchExcursions")
        return apiService.getExcursions(offset, limit)
    }

    override suspend fun fetchExcursion(id: Long): ExcursionResponse {
        Log.d("FetchingEx", "FetchExcursion")
        return apiService.getExcursion(id)
    }

    override suspend fun getExcursionFromDB(id: Long): Excursion? {
        Log.d("GettingEx", "GetExcursion")
        return excursionDao.getExcursionById(id)
    }

    override suspend fun deleteAllExcursionsFromExcursions() = excursionsDao.clearAll()

    override suspend fun deleteAllExcursionsFromExcursion() = excursionDao.clearAll()

    override suspend fun createExcursion(creatingExcursion: CreatingExcursion): ExcursionResponse {
        return apiService.createExcursion(creatingExcursion)
    }

    //вроде как не нужен, но на всякий случай оставлю
    override suspend fun searchExcursions(
        query: String,
        offset: Int,
        limit: Int
    ): ExcursionsResponse {
        Log.d("FetchingExs", "FetchExcursions")
        return apiService.searchExcursions(query, offset, limit)
    }
}