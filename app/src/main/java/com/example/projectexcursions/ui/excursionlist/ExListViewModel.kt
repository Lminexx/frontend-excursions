package com.example.projectexcursions.ui.excursionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.exlistrepo.ExcursionPagingSource
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExListViewModel(private val apiService: ApiService, private val repository: ExcursionRepository) : ViewModel() {

    val excursionsFromApi: Flow<PagingData<Excursion>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { ExcursionPagingSource(apiService) }
    ).flow
        .cachedIn(viewModelScope)

    val excursionsFromDatabase: List<Excursion> = repository.getAllExcursionsFromDB()

    fun saveExcursionsToDatabase(excursions: List<Excursion>) {
        viewModelScope.launch {
            repository.saveExcursionsToDB(excursions)
        }
    }

    fun loadExcursionsFromApi(offset: Int, limit: Int) {
        viewModelScope.launch {
            val response = repository.fetchExcursions(offset, limit)
            if (response.isSuccessful) {
                response.body()?.content?.let {
                    saveExcursionsToDatabase(it)
                }
            }
        }
    }
}
