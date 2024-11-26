package com.example.projectexcursions.modules

import android.content.Context
import com.example.projectexcursions.databases.OpenWorldDB
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.net.ApiClient
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): OpenWorldDB {
        return OpenWorldDB.getDatabase(context)
    }

    @Provides
    fun provideExcursions(db: OpenWorldDB): ExcursionDao {
        return db.excursionDao()
    }

    @Provides
    @Singleton
    fun provideExcursionRepository(
        apiClient: ApiClient,
        excursionDao: ExcursionDao
    ): ExcursionRepository {
        return ExcursionRepository(apiClient.instance, excursionDao)
    }
}