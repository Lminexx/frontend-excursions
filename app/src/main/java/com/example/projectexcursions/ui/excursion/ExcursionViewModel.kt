package com.example.projectexcursions.ui.excursion

import android.util.Log
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

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _excursion = MutableLiveData<Excursion?>()
    val excursion: LiveData<Excursion?> = _excursion

    fun loadExcursion(excursionId: Long) {
        viewModelScope.launch {
            try {
                if (repository.getExcursionFromDB(excursionId) != null) {
                    val excursionFromDB = repository.getExcursionFromDB(excursionId)
                    _excursion.value = excursionFromDB
                    Log.d("ExcursionInDB", "ExcExists")
                } else {
                    val response = repository.fetchExcursion(id = excursionId)
                    repository.saveExcursionToDB(response.excursion)
                    _excursion.value = response.excursion
                    Log.d("ExcursionIsnInDB", "FetchExcursion")
                }
            } catch (e: Exception) {
                Log.e("LoadExcursion", e.message!!)
                _excursion.value = null
            }
        }
    }
    fun clickComeback() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }
}
