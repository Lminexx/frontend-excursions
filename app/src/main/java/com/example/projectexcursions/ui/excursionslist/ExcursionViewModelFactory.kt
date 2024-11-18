package com.example.projectexcursions.ui.excursionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectexcursions.repositories.ExcursionRepository

class ExcursionViewModelFactory(
    private val excursionRepository: ExcursionRepository
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExcursionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExcursionViewModel(excursionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}