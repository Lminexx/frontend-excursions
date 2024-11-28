package com.example.projectexcursions.net

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepositoryImpl
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class ExcursionRemoteMediator @Inject constructor(
    private val repository: ExcursionRepositoryImpl
) : RemoteMediator<Int, Excursion>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Excursion>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                lastItem.id
            }
        }
        return try {
            val response = repository.fetchExcursions(page, state.config.pageSize)
            val excursions = response.content
            repository.saveExcursionsToDB(excursions)
            MediatorResult.Success(endOfPaginationReached = excursions.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}