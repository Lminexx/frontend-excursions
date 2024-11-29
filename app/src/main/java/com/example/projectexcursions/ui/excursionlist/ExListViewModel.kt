package com.example.projectexcursions.ui.excursionlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.projectexcursions.databases.daos.ExcursionDao
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ExcursionRemoteMediator
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ExListViewModel @Inject constructor(
    repository: ExcursionRepositoryImpl,
    excursionDao: ExcursionDao
    ) : ViewModel() {

    private val remoteMediator = ExcursionRemoteMediator(repository)

    private val _goToExcursion = MutableLiveData(false)
    val goToExcursion: LiveData<Boolean> get() = _goToExcursion

    var selectedExcursion: Excursion? = null

    @OptIn(ExperimentalPagingApi::class)
    val excursions: Flow<PagingData<Excursion>> = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        remoteMediator = remoteMediator,
        pagingSourceFactory = { excursionDao.getPagingSource() }
    ).flow.cachedIn(viewModelScope)

    fun clickExcursion(excursion: Excursion) {
        selectedExcursion = excursion
        _goToExcursion.value = true
    }

    fun goneToExcursion() {
        _goToExcursion.value = false
    }
}
