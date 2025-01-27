package com.example.projectexcursions.net

import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("user")
    suspend fun registerUser(@Body user: User): RegistrationResponse
    //todo отображение ошибок, которые отправляют нам бекенд

    @GET("excursion")
    suspend fun getExcursions(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("isFavorite") isFavorite: Boolean,
        @Query("myList") myList: Boolean
    ): ExcursionsResponse

    @POST("user/login")
    suspend fun authUser(@Body user: User): AuthResponse
    //todo отображение ошибок, которые отправляют нам бекенд

    @GET("excursion/{id}")
    suspend fun getExcursion(@Path("id") id: Long): ExcursionResponse

    @DELETE("excursion/{id}")
    suspend fun deleteExcursion(@Path("id") id: Long)

    @POST("excursion/create")
    suspend fun createExcursion(
        @Body creatingExcursion: CreatingExcursion
    ): ExcursionResponse

    @POST("excursion/{id}/favorite")
    suspend fun addFavorite(@Path("id") id: Long)

    @POST("excursion/{id}/unFavorite")
    suspend fun deleteFavorite(@Path("id") id: Long)

    //todo миграция с http на https
}