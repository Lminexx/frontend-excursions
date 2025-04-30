package com.example.projectexcursions.ui.filtration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject


class FiltrationViewModel @Inject constructor(): ViewModel() {
    private val _filter = MutableLiveData<Boolean>()
    val filter: LiveData<Boolean> get() = _filter

    fun clickFilter(){
        _filter.value = true
    }
}