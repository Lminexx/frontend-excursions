package com.example.projectexcursions.ui.excursion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExcursionViewModel @Inject constructor() : ViewModel() {

    private val _comeBackToMainActivity = MutableLiveData(false)
    val comeBackToMainActivity: LiveData<Boolean> get() = _comeBackToMainActivity

    fun clickComeback() {
        _comeBackToMainActivity.value = true
    }


    fun cameBack() {
        _comeBackToMainActivity.value = false
    }
}
