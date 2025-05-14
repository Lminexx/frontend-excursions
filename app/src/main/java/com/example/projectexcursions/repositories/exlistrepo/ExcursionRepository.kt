package com.example.projectexcursions.repositories.exlistrepo

import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.models.ModeratingExcursionsResponse
import com.example.projectexcursions.net.ExcursionResponse
import com.example.projectexcursions.net.ExcursionsResponse
import com.example.projectexcursions.net.PhotoResponse
import com.example.projectexcursions.net.RatingResponse
import com.example.projectexcursions.paging_sources.ExcursionPagingSource
import com.example.projectexcursions.paging_sources.FiltrationExcursionPagingSource
import com.example.projectexcursions.paging_sources.ModeratingExcursionsPagingSource
import com.example.projectexcursions.paging_sources.SearchExcursionPagingSource
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.math.BigDecimal

interface ExcursionRepository {
    fun excursionPagingSource(isFavorite: Boolean, isMine: Boolean): ExcursionPagingSource

    fun searchExcursionPagingSource(query: String, isMine: Boolean, isFavorite: Boolean): SearchExcursionPagingSource

    fun moderatingExcursionsPagingSource(): ModeratingExcursionsPagingSource

    fun filtrationExcursionPagingSource(rating: Float?, startDate:String?, endDate:String?, tags:List<String>, topic:String?,city:String?):
            FiltrationExcursionPagingSource

    suspend fun getAllExcursionsFromDB(): List<ExcursionsList>

    suspend fun saveExcursionsToDB(excursionsLists: List<ExcursionsList>)

    suspend fun saveExcursionToDB(excursion: Excursion)

    suspend fun fetchExcursions(offset: Int, limit: Int, isFavorite: Boolean, isMine: Boolean): Response<ExcursionsResponse>

    suspend fun fetchExcursion(id: Long): Response<ExcursionResponse>

    suspend fun deleteExcursion(id: Long)

    suspend fun getExcursionFromDB(id: Long): Excursion?

    suspend fun deleteAllExcursionsFromExcursions()

    suspend fun deleteAllExcursionsFromExcursion()

    suspend fun createExcursion(creatingExcursion: CreatingExcursion): Response<ExcursionResponse>

    suspend fun addFavorite(id:Long)

    suspend fun deleteFavorite(id:Long)

    suspend fun checkFav(excursionId: Long): Boolean

    suspend fun searchExcursions(query: String, offset: Int, limit: Int, isFavorite: Boolean, isMine: Boolean): Response<ExcursionsResponse>

    suspend fun uploadPhotos(files: List<MultipartBody.Part>, excursionId: RequestBody): Response<PhotoResponse>

    suspend fun loadPhotos(id: Long): Response<List<PhotoResponse>>


    suspend fun uploadRating(id:Long, rating: Float): Response<RatingResponse>

    suspend fun changeExcursionStatus(id: Long, status: String)

    suspend fun loadModeratingExcursions(offset: Int, limit: Int, status: String): Response<ModeratingExcursionsResponse>

    suspend fun editExcursion(id: Long, editingExcursion: CreatingExcursion)
}