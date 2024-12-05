package com.example.projectexcursions.modules

import androidx.recyclerview.widget.DiffUtil
import com.example.projectexcursions.adapters.ExcursionAdapter
import com.example.projectexcursions.models.ExcursionsList
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