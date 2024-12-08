package com.example.projectexcursions.ui.create_excursion

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectexcursions.models.Excursion // Предполагаем, что у вас есть такой класс.
import com.example.projectexcursions.net.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateExcursionViewModel @Inject constructor(
    private val apiService: ApiService // Внедрение API сервиса.
) : ViewModel() {

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    private val _createExcursion = MutableLiveData<Boolean>()
    val createExcursion: LiveData<Boolean> get() = _createExcursion

    private val _wantCreateExc = MutableLiveData<Boolean>()
    val wantCreateExc: LiveData<Boolean> get() = _wantCreateExc

    private val _creationMessage = MutableLiveData<String?>()
    val creationMessage: LiveData<String?> get() = _creationMessage

    // Метод для создания экскурсии, аналогичный методу регистрации
    fun createExcursion(title: String, description: String) {
        if (title.isBlank()) {
            _creationMessage.value = "Введите название"
            return
        }
        if (description.isBlank()) {
            _creationMessage.value = "Введите описание"
            return
        }

        viewModelScope.launch {
            try {
                val excursion = Excursion(title, description)
                val response = apiService.createExcursion(excursion)
                Log.d("CreateExcursion", "Response: $response")
                _creationMessage.value = "Экскурсия успешно создана"
                _createExcursion.value = true
            } catch (e: Exception) {
                Log.e("CreateError", "Ошибка при создании экскурсии: ${e.message}")
                _creationMessage.value = "Ошибка при создании: \n${e.message}"
                _createExcursion.value = false
            }
        }
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }

    fun clickCreateExcursion() {
        createExcursion("", "") // Замените на фактические данные или вызовите с параметрами извне.
    }

    fun excursionCreated() {
        _createExcursion.value = false
    }
}
