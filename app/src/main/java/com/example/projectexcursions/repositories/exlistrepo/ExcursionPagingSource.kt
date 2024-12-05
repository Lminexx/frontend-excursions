package com.example.projectexcursions.repositories.exlistrepo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ApiService
import retrofit2.HttpException
import java.io.IOException

class ExcursionPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, ExcursionsList>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ExcursionsList> {
        val position = params.key ?: 0
        return try {
            val response = apiService.getExcursions(offset = position, limit = params.loadSize)
                val excursions = response.content
                val pageInfo = response.page

                LoadResult.Page(
                    data = excursions,
                    prevKey = if (position == 0) null else position - 1,
                    nextKey = if (pageInfo.number < pageInfo.totalPages) position + 1 else null
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
        return state.anchorPosition
    }
}
