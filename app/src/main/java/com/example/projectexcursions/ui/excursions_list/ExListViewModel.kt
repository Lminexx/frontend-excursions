package com.example.projectexcursions.ui.excursions_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
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
class ExListViewModel @Inject constructor(
    repository: ExcursionRepository
) : ViewModel() {
    data class FiltrationData(
        val rating: Float? = null,
        val startDate: String? = null,
        val endDate: String? = null,
        val tags: List<String> = emptyList(),
        val minDuration: Int? = null,
        val maxDuration: Int? = null,
        val topic: String? = null,
        val city: String? = null
    )

    private val _goToExcursion = MutableLiveData(false)
    val goToExcursion: LiveData<Boolean> get() = _goToExcursion

    private val _filterData = MutableLiveData<FiltrationData>()
    val filterData: LiveData<FiltrationData> get() = _filterData

    private val searchExcursion = MutableStateFlow("")

    private val isSearching = MutableStateFlow(false)

    var selectedExcursionsList: ExcursionsList? = null

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    var excursions: Flow<PagingData<ExcursionsList>> = isSearching
        .flatMapLatest { searching ->
            if (searching) {
                searchExcursion
                    .debounce(1000)
                    .distinctUntilChanged()
                    .flatMapLatest { query ->
                        Pager(
                            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                            pagingSourceFactory = {
                                repository.searchExcursionPagingSource(
                                    query,
                                    isFavorite = false,
                                    isMine = false
                                )
                            }
                        ).flow
                    }
            } else {
                Pager(
                    config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                    pagingSourceFactory = {
                        repository.excursionPagingSource(
                            isFavorite = false,
                            isMine = false
                        )
                    }
                ).flow
            }
        }.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    var filterExcursions: Flow<PagingData<ExcursionsList>> = _filterData.asFlow()
        .flatMapLatest { filterData ->
            Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = {
                    repository.filtrationExcursionPagingSource(
                        filterData?.rating,
                        filterData?.startDate,
                        filterData?.endDate,
                        filterData?.tags ?: emptyList(),
                        filterData?.minDuration,
                        filterData?.maxDuration,
                        filterData?.topic,
                        filterData?.city,
                    )
                }
            ).flow
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

    fun setFiltrationData(rating: Float?, startDate:String?, endDate:String?, tags:List<String>, minDuration:Int?, maxDuration:Int?, topic:String?,city:String?){
        _filterData.value=
            FiltrationData(rating, startDate, endDate, tags, minDuration, maxDuration, topic,city)
    }
}