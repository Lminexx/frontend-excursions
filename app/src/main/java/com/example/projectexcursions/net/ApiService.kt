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
    fun registerUser(@Body user: User): Call<String>
    //todo отображение ошибок, которые отправляют нам бекенд
    @GET("excursion")
    fun getExcursions(
        @Query("offset") page: Int,
        @Query("limit") size: Int
    ): Response<ExcursionResponse>
}