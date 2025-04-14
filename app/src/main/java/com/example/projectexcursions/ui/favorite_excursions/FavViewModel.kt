package com.example.projectexcursions.ui.favorite_excursions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class FavViewModel @Inject constructor(
    repository: ExcursionRepository
) : ViewModel() {

    private val _goToExcursion = MutableLiveData(false)
    val goToExcursion: LiveData<Boolean> get() = _goToExcursion

    var selectedExcursionsList: ExcursionsList? = null

    private val searchExcursion = MutableStateFlow("")

    private val isSearching = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    var favExcursions: Flow<PagingData<ExcursionsList>> = isSearching
        .flatMapLatest { searching ->
            if (searching) {
                searchExcursion
                    .debounce(1000)
                    .distinctUntilChanged()
                    .flatMapLatest { query ->
                        Pager(
                            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                            pagingSourceFactory = { repository.searchExcursionPagingSource(query, isFavorite = true, isMine = false) }
                        ).flow
                    }
            } else {
                Pager(
                    config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                    pagingSourceFactory = { repository.excursionPagingSource(isFavorite = true, isMine = false) }
                ).flow
            }
        }.cachedIn(viewModelScope)

    fun clickExcursion(excursionsList: ExcursionsList) {
        selectedExcursionsList = excursionsList
        _goToExcursion.value = true
    }

    fun goneToExcursion() {
        _goToExcursion.value = false
    }

    fun searchExcursionsQuery(query: String) {
        isSearching.value = query.isNotEmpty()
        searchExcursion.value = query
    }

    fun resetSearch() {
        isSearching.value = false
        searchExcursion.value = ""
    }
}