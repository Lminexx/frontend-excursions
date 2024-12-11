package com.example.projectexcursions.ui.excursion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExcursionViewModel @Inject constructor(
    private val apiService: ApiService,
    private val repository: ExcursionRepository
) : ViewModel() {

    private val _comeBackToMainActivity = MutableLiveData(false)
    val comeBackToMainActivity: LiveData<Boolean> get() = _comeBackToMainActivity

    private val _excursion = MutableLiveData<Excursion?>()
    val excursion: LiveData<Excursion?> = _excursion
    fun loadExcursion(excursionId: Long) {
        viewModelScope.launch {
            try {
                if (repository.getExcursionFromDB(excursionId) != null) {
                    val excursionFromDB = repository.getExcursionFromDB(excursionId)
                    _excursion.postValue(excursionFromDB)
                } else {
                    val response = apiService.getExcursion(excursionId)
                    repository.saveExcursionToDB(response.excursion)
                    _excursion.postValue(response.excursion)
                }
            } catch (e: Exception) {
                _excursion.postValue(null)
            }
        }
    }
    fun clickComeback() {
        _comeBackToMainActivity.value = true
    }

    fun cameBack() {
        _comeBackToMainActivity.value = false
    }
}
