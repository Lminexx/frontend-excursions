package com.example.projectexcursions.net

import com.example.projectexcursions.models.CreatingExcursion
import com.example.projectexcursions.models.ModeratingExcursionsResponse
import com.example.projectexcursions.models.PlaceItem
import com.example.projectexcursions.models.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

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
    suspend fun uploadPhotos(
        @Part files: List<MultipartBody.Part>,
        @Part("excursionId") excursionId: RequestBody
    ): PhotoResponse

    @GET("excursion/photo/{id}")
    suspend fun loadPhotos(@Path("id") id: Long): List<PhotoResponse>

    @POST("user/validToken")
    suspend fun validateToken()

    @Multipart
    @POST("user/avatar/upload")
    suspend fun uploadAvatar(
        @Part("fileName") fileName: RequestBody,
        @Part file: MultipartBody.Part
    ):AuthResponse

    @POST("excursion/points/{excursionId}")
    suspend fun uploadPlaceItems(
        @Path("excursionId") id: Long,
        @Body places: List<PlaceItem>
    )

    @GET("excursion/points/{id}")
    suspend fun loadPlaces(@Path("id") id: Long): List<PlaceItem>


    @POST("excursion/rating")
    suspend fun uploadRating(
        @Query("excursionId") excursionId: Long,
        @Query("ratingValue") ratingValue: Float
    ) : RatingResponse

    @PUT("excursion/moderation/{id}/status")
    suspend fun changeExcursionStatus(
        @Path("id") id: Long,
        @Query("status") status: String)

    @GET("excursion/moderation")
    suspend fun loadModeratingExcursions(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
        @Query("status") status: String
    ): ModeratingExcursionsResponse
}