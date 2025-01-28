package com.example.projectexcursions.modules

import com.example.projectexcursions.net.ExcursionRemoteMediator
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.yandex.mapkit.search.BusinessFilter.BooleanValue
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ExcursionRemoteMediatorModule {

    @Provides
    fun provideExRemoteMediator(
        repo: ExcursionRepository
    ): ExcursionRemoteMediator {
        return ExcursionRemoteMediator(repo)
    }
}