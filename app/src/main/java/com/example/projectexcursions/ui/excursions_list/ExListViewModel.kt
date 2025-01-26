package com.example.projectexcursions.ui.excursions_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.projectexcursions.databases.daos.ExcursionsDao
import com.example.projectexcursions.models.ExcursionsList
import com.example.projectexcursions.net.ExcursionRemoteMediator
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
class ExListViewModel @Inject constructor(
    repository: ExcursionRepository,
    excursionsDao: ExcursionsDao
    ): ViewModel() {

    private val remoteMediator = ExcursionRemoteMediator(repository)

    private val _goToExcursion = MutableLiveData(false)
    val goToExcursion: LiveData<Boolean> get() = _goToExcursion

    private val searchExcursion = MutableStateFlow("")

    private val isSearching = MutableStateFlow(false)

    var selectedExcursionsList: ExcursionsList? = null

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class, ExperimentalPagingApi::class)
    var excursions: Flow<PagingData<ExcursionsList>> = isSearching
        .flatMapLatest { searching ->
            if (searching) {
                searchExcursion
                    .debounce(1000)
                    .distinctUntilChanged()
                    .flatMapLatest { query ->
                        Pager(
                            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                            pagingSourceFactory = { repository.searchExcursionPagingSource(query, isMine = false) }
                        ).flow
                    }
            } else {
                Pager(
                    config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                    pagingSourceFactory = { repository.excursionPagingSource(false) },
                    remoteMediator = remoteMediator
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