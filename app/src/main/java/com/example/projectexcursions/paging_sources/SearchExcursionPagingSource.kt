package com.example.projectexcursions.paging_sources

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ApiService
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SearchExcursionPagingSource @Inject constructor(
    private val apiService: ApiService,
    private val query: String,
    private val isMine: Boolean,
    private val isFavorite: Boolean
): PagingSource<Int, ExcursionsList>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ExcursionsList> {
        val position = params.key ?: 0
        Log.d("SearchPagingSource", "Loading page: $position")
        Log.d("SearchPaging", "Query: ${query.trim()}")
        Log.d("SearchPaging", "Offset: $position")
        Log.d("SearchPaging", "limit: ${params.loadSize}")
        return try {
            val response = apiService.searchExcursions(query = query, offset = position, limit = params.loadSize, isFavorite = isFavorite, isMine = isMine)
            Log.d("SearchPagingSource", "Query: $query")
            val excursions = response.content
            Log.d("PagingSource", "$excursions")
            val pageInfo = response.page
            val prevKey = if (position == 0) null else position - 1
            val nextKey = if (pageInfo.number < pageInfo.totalPages) position + 1 else null
            Log.d("PagingSource3", "NextKey: $nextKey, PrevKey: $prevKey")

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