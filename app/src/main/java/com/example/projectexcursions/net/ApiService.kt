package com.example.projectexcursions.net

import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("user")
    suspend fun registerUser(@Body user: User): RegistrationResponse

    @GET("excursion")
    suspend fun getExcursions(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("isFavorite") isFavorite: Boolean
    ): ExcursionsResponse

    @POST("user/login")
    suspend fun authUser(@Body user: User): AuthResponse

    @GET("excursion/{id}")
    suspend fun getExcursion(@Path("id") id: Long): ExcursionResponse

    @POST("excursion/create")
    suspend fun createExcursion(
        @Body creatingExcursion: CreatingExcursion
    ): ExcursionResponse

    @GET("excursion/search")
    suspend fun searchExcursions(
        @Query("query") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("isFavorite") isFavorite: Boolean
    ): ExcursionsResponse
}