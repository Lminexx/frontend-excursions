package com.example.projectexcursions.ui.registration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegViewModel: ViewModel() {
    //TODO обзяательная проверка логина и пароля, если такие существуют - выдать исключение с сообщшением или просто сообщение с ошибкой

    private val _regStatus = MutableLiveData<Boolean>()
    val regStatus: LiveData<Boolean> get() = _regStatus

    fun reg(login: String, password: String) {
        //todo скинуть логин и пароль бекенду
    }

    fun clickRegister() {
        //TODO("Not yet implemented")
    }
}