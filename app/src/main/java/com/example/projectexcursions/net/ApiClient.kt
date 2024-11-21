package com.example.projectexcursions.net

import com.example.projectexcursions.MyApplication
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8083/api/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Логируем тело запроса и ответа
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Логирование HTTP запросов и ответов
        .addInterceptor { chain ->
            val token = MyApplication.getAuthToken() // Получаем токен через MyApplication
            val originalRequest = chain.request()  // Исходный запрос
            val requestBuilder = originalRequest.newBuilder()

            if (token != null) {
                requestBuilder.addHeader("Authorization", "Bearer $token") // Добавляем заголовок авторизации
            }

            val request = requestBuilder.build()  // Строим новый запрос с токеном
            chain.proceed(request)  // Продолжаем выполнение запроса
        }
        .connectTimeout(30, TimeUnit.SECONDS)  // Таймаут на соединение
        .readTimeout(30, TimeUnit.SECONDS)     // Таймаут на чтение
        .build()

    // Создание Retrofit клиента с нужным конвертером
    private val json = Json { ignoreUnknownKeys = true }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Используем наш OkHttpClient с логированием
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())) // Конвертер JSON
            .build()
            .create(ApiService::class.java) // Создаем сервис для работы с API
    }

    fun getAuthToken(): String? {
        return MyApplication.getAuthToken()  // Можно получить токен через MyApplication
    }
}