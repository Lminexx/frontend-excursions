package com.example.projectexcursions.net

import com.example.projectexcursions.models.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("user")
    suspend fun registerUser(@Body user: User): RegistrationResponse
    //todo отображение ошибок, которые отправляют нам бекенд
    @GET("excursion")
    suspend fun getExcursions(
        @Query("offset") page: Int,
        @Query("limit") limit: Int
    ): ExcursionResponse
    @POST("user/login")
    suspend fun authUser(@Body user: User): AuthResponse
    //todo отображение ошибок, которые отправляют нам бекенд


    //todo миграция с http на https и
}