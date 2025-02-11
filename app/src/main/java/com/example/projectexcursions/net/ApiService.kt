package com.example.projectexcursions.net

import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("user")
    suspend fun registerUser(@Body user: User): RegistrationResponse

    @GET("excursion")
    suspend fun getExcursions(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("isFavorite") isFavorite: Boolean,
        @Query("myList") isMine: Boolean
    ): ExcursionsResponse

    @POST("user/login")
    suspend fun authUser(@Body user: User): AuthResponse

    @GET("excursion/{id}")
    suspend fun getExcursion(@Path("id") id: Long): ExcursionResponse


    @DELETE("excursion/{id}")
    suspend fun deleteExcursion(@Path("id") id: Long): Response<Unit>

    @POST("excursion/create")
    suspend fun createExcursion(
        @Body creatingExcursion: CreatingExcursion
    ): ExcursionResponse

    @POST("excursion/{id}/favorite")
    suspend fun addFavorite(@Path("id") id: Long)

    @POST("excursion/{id}/unFavorite")
    suspend fun deleteFavorite(@Path("id") id: Long)

    //todo миграция с http на https
    @GET("excursion/search")
    suspend fun searchExcursions(
        @Query("query") query: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("isFavorite") isFavorite: Boolean,
        @Query("myList") isMine: Boolean
    ): ExcursionsResponse

    @Multipart
    @POST("excursion/photo/upload")
    suspend fun uploadPhoto(
        @Part("fileName") fileName: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("excursionId") excursionId: RequestBody
    ): PhotoResponse

    @GET("excursion/photo/{id}")
    suspend fun downloadPhoto(

    )
}