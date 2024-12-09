package com.example.projectexcursions.modules

import android.content.Context
import com.example.projectexcursions.databases.OpenWorldDB
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.databases.daos.ExcursionsDao
import com.example.projectexcursions.databases.daos.TokenDao
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepositoryImpl
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.example.projectexcursions.repositories.tokenrepo.TokenRepositoryImpl
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
    fun provideExcursions(db: OpenWorldDB): ExcursionsDao {
        return db.excursionsDao()
    }

    @Provides
    fun provideExcursion(db: OpenWorldDB): ExcursionDao {
        return db.excursionDao()
    }

    @Provides
    fun provideToken(db: OpenWorldDB): TokenDao {
        return db.tokenDao()
    }

    @Provides
    @Singleton
    fun provideExcursionRepository(
        apiService: ApiService,
        excursionsDao: ExcursionsDao,
        excursionDao: ExcursionDao
    ): ExcursionRepository {
        return ExcursionRepositoryImpl(apiService, excursionsDao, excursionDao)
    }

    @Provides
    @Singleton
    fun provideTokenRepository(
        apiService: ApiService,
        tokenDao: TokenDao
    ): TokenRepository {
        return TokenRepositoryImpl(apiService, tokenDao)
    }
}