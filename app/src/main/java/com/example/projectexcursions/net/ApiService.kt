package com.example.projectexcursions.net

import com.example.projectexcursions.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("user/register")
    fun registerUser(@Body user: User): Call<RegistrationResponse>
    //todo отображение ошибок, которые отправляют нам бекенд
    @GET("excursions")
    fun getExcursions(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<ExcursionResponse>
}