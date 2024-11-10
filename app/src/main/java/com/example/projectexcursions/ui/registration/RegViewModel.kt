package com.example.projectexcursions.ui.registration

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText


class RegViewModel: ViewModel() {
    //TODO обзяательная проверка логина и пароля, если такие существуют - выдать исключение с сообщшением или просто сообщение с ошибкой

    private val _inputLogin = MutableLiveData<String>()
    val inputLogin: LiveData<String> get() = _inputLogin

    private val _regStatus = MutableLiveData<Boolean>()
    val regStatus: LiveData<Boolean> get() = _regStatus

    private val _wantComeBack = MutableLiveData<Boolean>()
    val wantComeBack: LiveData<Boolean> get() = _wantComeBack

    fun reg(login: String, password: String) {
        //todo скинуть логин и пароль бекенду
    }

    fun clickRegister() {
        _regStatus.value = true
    }

    fun clickComeBack() {
        _wantComeBack.value = true
    }

    fun cameBack() {
        _wantComeBack.value = false
    }

    fun updateInputLogin(login: String) {
        _inputLogin.value = login
    }
}