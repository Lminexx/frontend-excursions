package com.example.projectexcursions.net

import com.example.projectexcursions.user.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("/api/v1/user/register")
    fun registerUser(@Body user: User): Call<RegistrationResponse>
    //todo отображение ошибок, которые отправляют нам бекенд
}