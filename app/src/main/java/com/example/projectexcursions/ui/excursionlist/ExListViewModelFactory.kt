package com.example.projectexcursions.ui.excursionslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.projectexcursions.net.ApiClient
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.ui.excursionlist.ExListViewModel

class ExListViewModelFactory(
    private val apiClient: ApiClient,
    private val repository: ExcursionRepository
): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExListViewModel::class.java)) {
            val apiService = apiClient.instance
            @Suppress("UNCHECKED_CAST")
            return ExListViewModel(apiService, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}