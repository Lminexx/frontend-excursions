package com.example.projectexcursions.ui.created_excursions_list

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
class CreatedExListViewModel @Inject constructor(
    repository: ExcursionRepository,
): ViewModel() {

    private val _goToExcursion = MutableLiveData<Boolean>()
    val goToExcursion: LiveData<Boolean> get() = _goToExcursion

    private val isSearching = MutableStateFlow(false)

    private val searchExcursion = MutableStateFlow("")

    var selectedExcursionsList: ExcursionsList? = null

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    var createdExcursions: Flow<PagingData<ExcursionsList>> = isSearching
        .flatMapLatest { searching ->
            if (searching) {
                searchExcursion
                    .debounce(1000)
                    .distinctUntilChanged()
                    .flatMapLatest { query ->
                        Pager(
                            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                            pagingSourceFactory = { repository.searchExcursionPagingSource(query) }
                        ).flow
                    }
            } else {
                Pager(
                    config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                    pagingSourceFactory = { repository.excursionPagingSource(true) }
                ).flow
            }
        }.cachedIn(viewModelScope)

    fun goneToExcursion() {
        _goToExcursion.value = false
    }

    fun resetSearch() {
        isSearching.value = false
        searchExcursion.value = ""
    }

    fun searchExcursionsQuery(query: String) {
        isSearching.value = query.isNotEmpty()
        searchExcursion.value = query
    }

    fun clickExcursion(excursionsList: ExcursionsList) {
        selectedExcursionsList = excursionsList
        _goToExcursion.value = true
    }
}