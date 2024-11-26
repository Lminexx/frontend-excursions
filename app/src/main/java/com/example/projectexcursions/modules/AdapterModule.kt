package com.example.projectexcursions.modules

import com.example.projectexcursions.adapters.ExcursionAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)

object AdapterModule {

    @Provides
    fun provideExcursionAdapter(): ExcursionAdapter {
        return ExcursionAdapter()
    }
}