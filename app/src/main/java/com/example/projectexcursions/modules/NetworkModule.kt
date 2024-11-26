package com.example.projectexcursions.modules

import com.example.projectexcursions.net.ApiClient
import com.example.projectexcursions.net.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideApiService(apiClient: ApiClient): ApiService {
        return apiClient.instance
    }

    @Provides
    fun provideApiClient(): ApiClient {
        return ApiClient
    }
}
