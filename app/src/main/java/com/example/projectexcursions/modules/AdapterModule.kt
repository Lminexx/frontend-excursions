package com.example.projectexcursions.modules

import androidx.recyclerview.widget.DiffUtil
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.models.Excursion
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object AdapterModule {

    @Provides
    fun provideExcursionAdapter(diffCallback: DiffUtil.ItemCallback<Excursion>): ExcursionAdapter {
        return ExcursionAdapter(diffCallback)
    }

    @Provides
    fun provideDiffUtilCallback(): DiffUtil.ItemCallback<Excursion> {
        return object : DiffUtil.ItemCallback<Excursion>() {
            override fun areItemsTheSame(oldItem: Excursion, newItem: Excursion): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Excursion, newItem: Excursion): Boolean {
                return oldItem == newItem
            }
        }
    }
}