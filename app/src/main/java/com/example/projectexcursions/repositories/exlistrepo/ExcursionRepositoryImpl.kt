package com.example.projectexcursions.repositories.exlistrepo

import android.util.Log
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.databases.daos.ExcursionsDao
import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.net.ExcursionResponse
import com.example.projectexcursions.net.ExcursionsResponse
import com.example.projectexcursions.net.PhotoResponse
import com.example.projectexcursions.net.RatingResponse
import com.example.projectexcursions.paging_sources.ExcursionPagingSource
import com.example.projectexcursions.paging_sources.SearchExcursionPagingSource
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.math.BigDecimal
import javax.inject.Inject

class ExcursionRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val excursionsDao: ExcursionsDao,
    private val excursionDao: ExcursionDao
) : ExcursionRepository {

    override fun excursionPagingSource(isFavorite:Boolean, isMine: Boolean) = ExcursionPagingSource(apiService, isFavorite, isMine)

    override fun searchExcursionPagingSource(query: String, isMine: Boolean, isFavorite: Boolean) = SearchExcursionPagingSource(apiService, query, isMine, isFavorite)

    override suspend fun getAllExcursionsFromDB() = excursionsDao.getAllExcursions()

    override suspend fun saveExcursionsToDB(excursionsLists: List<ExcursionsList>) {
        Log.d("InsertExs", "InsertExcursions")
        excursionsDao.insertAll(excursionsLists)
    }

    override suspend fun saveExcursionToDB(excursion: Excursion) {
        Log.d("InsertEx", "InsertExcursion")
        excursionDao.insert(excursion)
    }

    override suspend fun fetchExcursions(offset: Int, limit: Int, isFavorite: Boolean, isMine: Boolean): ExcursionsResponse {
        Log.d("FetchingExs", "FetchExcursions")
        return apiService.getExcursions(offset, limit, isFavorite, isMine)
    }

    override suspend fun fetchExcursion(id: Long): ExcursionResponse {
        Log.d("FetchingEx", "Начинаем запрос экскурсии с ID: $id")
        try {
            val response = apiService.getExcursion(id)
            Log.d("FetchingExResponse", "Получен ответ: $response")
            Log.d("FetchingExDetails", "ID: ${response.id}, Title: ${response.title}, User: ${response.user}")
            return response
        } catch (e: Exception) {
            Log.e("FetchingExError", "Ошибка при запросе экскурсии: ${e.message}")
            Log.e("FetchingExStackTrace", "Стек вызовов: ${e.stackTraceToString()}")
            throw e
        }
    }

    override suspend fun deleteExcursion(id: Long){
        Log.d("DeleteEx", "DeleteExcursion")
        val response = apiService.deleteExcursion(id)
        if (response.isSuccessful) {
            val responseId = response.message()
            excursionDao.deleteExcursion(id)
            Log.d("DeleteExResponse", responseId)
        } else {
            Log.d("DeleteExNotResponse", "PisyaPopa(")
        }
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

    override suspend fun addFavorite(id: Long) {
        Log.d("FavoriteExcursion", "addFavorite")
        apiService.addFavorite(id)
        excursionDao.addFavorite(id)
    }

    override suspend fun deleteFavorite(id: Long) {
        Log.d("FavoriteExcursion", "deleteFavorite")
        apiService.deleteFavorite(id)
        excursionDao.deleteFavorite(id)
    }

    override suspend fun checkFav(excursionId: Long): Boolean {
        val excursion = excursionDao.getExcursionById(excursionId)
        return excursion!!.favorite
    }

    //вроде как не нужен, но на всякий случай оставлю
    override suspend fun searchExcursions(
        query: String,
        offset: Int,
        limit: Int,
        isFavorite: Boolean,
        isMine: Boolean
    ): ExcursionsResponse {
        Log.d("FetchingExs", "FetchExcursions")
        return apiService.searchExcursions(query, offset, limit, isFavorite, isMine)
    }

    override suspend fun uploadPhotos(files: List<MultipartBody.Part>, excursionId: RequestBody): PhotoResponse {
        return apiService.uploadPhotos(files, excursionId)
    }

    override suspend fun loadPhotos(id: Long): List<PhotoResponse> {
        return apiService.loadPhotos(id)
    }

    override suspend fun uploadRating(id:Long,rating:BigDecimal) : RatingResponse{
        return apiService.uploadRating(id, rating)
    }
}