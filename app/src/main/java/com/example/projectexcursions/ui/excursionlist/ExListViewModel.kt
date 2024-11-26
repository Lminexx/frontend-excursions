package com.example.projectexcursions.ui.excursionlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExListViewModel @Inject constructor(private val repository: ExcursionRepository) : ViewModel() {

    val excursionsFromApi: Flow<PagingData<Excursion>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { repository.getExcursionsPaging() }
    ).flow.cachedIn(viewModelScope)

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
