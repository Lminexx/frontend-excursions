package com.example.projectexcursions.repositories.exlistrepo

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ExcursionPagingSource @Inject constructor(
    private val apiService: ApiService
): PagingSource<Int, ExcursionsList>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ExcursionsList> {
        val position = params.key ?: 0
        Log.d("PagingSource", "Loading page: $position")
        Log.d("Paging", "Offset: $position")
        Log.d("Paging", "limit: ${params.loadSize}")
        return try {
            val response = apiService.getExcursions(offset = position, limit = params.loadSize, favoriteFlag = false)
            val excursions = response.content
            val pageInfo = response.page
            val prevKey = if (position == 0) null else position - 1
            val nextKey = if (pageInfo.number < pageInfo.totalPages) position + 1 else null
            Log.d("PagingSource", "NextKey: $nextKey, PrevKey: $prevKey")

            LoadResult.Page(
                    data = excursions,
                    prevKey = prevKey,
                    nextKey = nextKey
                )
        } catch (exception: IOException) {
            LoadResult.Error(Exception("Ошибка сети: ${exception.message}", exception))
        } catch (exception: HttpException) {
            LoadResult.Error(Exception("HTTP ошибка: ${exception.message()}", exception))
        } catch (exception: Exception) {
            LoadResult.Error(Exception("Неизвестная ошибка: ${exception.message}", exception))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ExcursionsList>): Int? {
        return state.anchorPosition?.let {anchorPosition ->
            val page = state.closestPageToPosition(anchorPosition)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }
}
