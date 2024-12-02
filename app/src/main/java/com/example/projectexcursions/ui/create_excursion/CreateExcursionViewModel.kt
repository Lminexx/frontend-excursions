package com.example.projectexcursions.ui.create_excursion

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateExcursionViewModel @Inject constructor(): ViewModel() {

    //для отслеживания перемещения юзера
    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    //для отслеживания состояния создания экскурсии
    private val _createExcursion = MutableLiveData<Boolean>()
    val createExcursion: LiveData<Boolean> get() = _createExcursion

    //для отслеживания клика на кнопку
    private val _wantCreateExc = MutableLiveData<Boolean>()
    val wantCreateExc: LiveData<Boolean> get() = _wantCreateExc

    fun createExcursion(title: String, description: String) {
        //todo здесь реализуй, собсна, логику воровки и отправки экскурсий, в качестве примера можешь использовать регистрацию
        //_createExcursion написал для того, чтобы можно было отслеживать состояние создания экскурсии, но можешь это убрать, если не надо
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }

    fun clickCreateExcursion() {
        _createExcursion.value = true
    }

    fun excursionCreated() {
        _createExcursion.value = false
    }
}