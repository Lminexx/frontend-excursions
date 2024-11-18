package com.example.projectexcursions.ui.excursionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.dbs.entities.ExcursionEntity
import com.example.projectexcursions.repositories.ExcursionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ExcursionViewModel(private val repository: ExcursionRepository): ViewModel() {
    private val _excursions = MutableStateFlow<List<ExcursionEntity>>(emptyList())
    val excursions: MutableStateFlow<List<ExcursionEntity>> = _excursions

    fun loadExcursions(page: Int, size: Int) {
        viewModelScope.launch {
            val localExcursions = repository.getExcursionsFromDb()
            if (localExcursions.isNotEmpty()) {
                _excursions.value = localExcursions
            } else {
                val newExcursions = repository.loadExcursions(page, size)
                _excursions.value = newExcursions
            }
        }
    }
}