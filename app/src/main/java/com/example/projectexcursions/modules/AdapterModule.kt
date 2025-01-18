package com.example.projectexcursions.modules

import androidx.recyclerview.widget.DiffUtil
import com.example.projectexcursions.adapter.ExcursionAdapter
import com.example.projectexcursions.adapter.FavAdapter
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.models.FavExcursionsList
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@Module
@InstallIn(FragmentComponent::class)
object AdapterModule {

    @Provides
    fun provideExcursionAdapter(diffCallback: DiffUtil.ItemCallback<ExcursionsList>): ExcursionAdapter {
        return ExcursionAdapter(diffCallback)
    }

    @Provides
    fun provideFunAdapter(diffCallback: DiffUtil.ItemCallback<FavExcursionsList>): FavAdapter{
        return FavAdapter(diffCallback)
    }

    @Provides
    fun provideDiffUtilCallback(): DiffUtil.ItemCallback<ExcursionsList> {
        return object : DiffUtil.ItemCallback<ExcursionsList>() {
            override fun areItemsTheSame(oldItem: ExcursionsList, newItem: ExcursionsList): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ExcursionsList, newItem: ExcursionsList): Boolean {
                return oldItem == newItem
            }
        }
    }
}