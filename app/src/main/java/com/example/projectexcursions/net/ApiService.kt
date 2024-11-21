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
    fun registerUser(@Body user: User): Call<Void>
    //todo отображение ошибок, которые отправляют нам бекенд
    @GET("excursion")
    fun getExcursions(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): Response<ExcursionResponse>
    @POST("user/login")
    fun authUser(@Body user: User): Call<AuthResponse>
    //todo отображение ошибок, которые отправляют нам бекенд
}