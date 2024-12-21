package com.example.projectexcursions.net

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ExcursionRemoteMediator @Inject constructor(
    private val repository: ExcursionRepository
) : RemoteMediator<Int, ExcursionsList>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ExcursionsList>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                repository.deleteAllExcursionsFromExcursions()
                0
            }
            LoadType.PREPEND -> {
                Log.d("LoadType", "LoadTypePrepended")
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                Log.d("RemoteMediator", "Pages size: ${state.pages.size}")
                state.pages.forEachIndexed { index, page ->
                    Log.d("RemoteMediator", "Page $index, NextKey: ${page.nextKey}, PrevKey: ${page.prevKey}")
                }
                Log.d("LoadType", "LoadTypeAppended")
                val nextKey = state.pages.lastOrNull()?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                Log.d("NextKey", "$nextKey")
                nextKey
            }
        }
        return try {
            Log.d("Page", "$page")
            val response = repository.fetchExcursions(page, state.config.pageSize)
            Log.d("ExcursionsGetter", "$response")
            val excursions = response.content
            Log.d("ExcursionsToSave", "Page: $page, Excursions: ${excursions.size}")
            repository.saveExcursionsToDB(excursions)
            Log.d("ExcursionRemoting", "SaveExcs")
            MediatorResult.Success(endOfPaginationReached = excursions.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}