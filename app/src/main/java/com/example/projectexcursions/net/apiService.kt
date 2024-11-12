package com.example.projectexcursions.net

import com.example.projectexcursions.user.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface apiService {
    @POST("/api/v1/user/register")
    fun registerUser(@Body user: User): Call<Void>

}