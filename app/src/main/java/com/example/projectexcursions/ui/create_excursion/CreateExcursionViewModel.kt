package com.example.projectexcursions.ui.create_excursion

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.models.Excursion
import com.example.projectexcursions.net.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateExcursionViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _validationMessage = MutableLiveData<String?>()
    val validationMessage: LiveData<String?> get() = _validationMessage

    private val _creationMessage = MutableLiveData<String?>()
    val creationMessage: LiveData<String?> get() = _creationMessage

    private val _createExcursionStatus = MutableLiveData<Boolean>()
    val createExcursionStatus: LiveData<Boolean> get() = _createExcursionStatus

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    fun validateAndCreateExcursion(title: String, description: String, userId: String) {
        when {
            title.isBlank() -> _validationMessage.value = "Введите название"
            description.isBlank() -> _validationMessage.value = "Введите описание"
            else -> {
                createExcursion(title, description, userId)
            }
        }
    }

    private fun createExcursion(title: String, description: String, userId: String) {
        viewModelScope.launch {
            try {
                val excursion = Excursion(title, description)
                val response = apiService.createExcursion(excursion, userId)
                Log.d("CreateExcursion", "Response: $response")
                _creationMessage.value = "Экскурсия успешно создана"
                _createExcursionStatus.value = true
            } catch (e: Exception) {
                Log.e("CreateError", "Ошибка при создании экскурсии: ${e.message}")
                _creationMessage.value = "Ошибка при создании: \n${e.message}"
                _createExcursionStatus.value = false
            }
        }
    }

    fun clickCreateButton(title: String, description: String, userId: String) {
        validateAndCreateExcursion(title, description)
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }

    fun excursionCreated() {
        _createExcursionStatus.value = false
    }
}
