package com.example.projectexcursions.repositories.exlistrepo

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import retrofit2.HttpException
import java.io.IOException
class ExcursionPagingSource(
    private val apiService: ApiService
) : PagingSource<Int, Excursion>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Excursion> {
        val position = params.key ?: 0
        return try {
            val response = apiService.getExcursions(offset = position, limit = params.loadSize)
            if (response.isSuccessful) {
                val excursions = response.body()?.content ?: emptyList()
                val pageInfo = response.body()?.page

                LoadResult.Page(
                    data = excursions,
                    prevKey = if (position == 0) null else position - 1,
                    nextKey = if ((pageInfo?.number ?: 0) < (pageInfo?.totalPages ?: 0)) position + 1 else null
                )
            } else {
                LoadResult.Error(Exception("Ошибка при получении данных"))
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Excursion>): Int? {
        return state.anchorPosition
    }
}
