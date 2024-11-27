package com.example.projectexcursions.modules

import android.content.Context
import com.example.projectexcursions.databases.OpenWorldDB
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.net.ApiService
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
        apiService: ApiService,
        excursionDao: ExcursionDao
    ): ExcursionRepository {
        return ExcursionRepository(apiService, excursionDao)
    }
}