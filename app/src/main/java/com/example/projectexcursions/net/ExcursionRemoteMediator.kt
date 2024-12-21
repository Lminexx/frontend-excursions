package com.example.projectexcursions.net

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
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
                0
            }
            LoadType.PREPEND -> {
                Log.d("LoadType", "LoadTypePrepended")
                return MediatorResult.Success(endOfPaginationReached = true)
            }
            LoadType.APPEND -> {
                Log.d("LoadType", "LoadTypeAppended")
                val pageCount: Int = if (state.firstItemOrNull() != null) {
                    state.firstItemOrNull()!!.id.toInt().div(10)
                } else {
                    0
                }
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                pageCount.minus(lastItem.id.toInt().div(10))
            }
        }.toInt()
        return try {
            val response = repository.fetchExcursions(page, state.config.pageSize)
            Log.d("ExcursionsGetter", "Get excursions")
            val excursions = response.content
            repository.saveExcursionsToDB(excursions)
            Log.d("SaveExc",  "SaveExc")
            MediatorResult.Success(endOfPaginationReached = excursions.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}