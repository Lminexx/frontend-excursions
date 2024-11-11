package com.example.projectexcursions.ui.auth

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.projectexcursions.user.User

class AuthViewModel: ViewModel() {

    private val _loginStatus = MutableLiveData<Boolean>()
    val loginStatus: LiveData<Boolean> get() = _loginStatus
    private val _wantReg = MutableLiveData<Boolean>()
    val wantReg: LiveData<Boolean> get() = _wantReg
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message
    private val user:User = User("","")

    val loginTextWatcher: TextWatcher get() = object:TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            user.setLogin(s.toString())
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            TODO("Not yet implemented")
        }

        override fun afterTextChanged(s: Editable?) {
            TODO("Not yet implemented")
        }

    }
    val passwordTextWatcher: TextWatcher get() = object:TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            user.setPass(s.toString())
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            TODO("Not yet implemented")
        }

        override fun afterTextChanged(s: Editable?) {
            TODO("Not yet implemented")
        }

    }
    fun login(login: String, password: String) {
        //todo скинуть логин и пароль бекенду
    }

    fun clickAuth() {
        if (user.isDataValid)
            _message.value = "Вход выполнен успешно"
        else
            _message.value = "Ошибка входа: проверьте данные"
    }

    fun clickRegister() {
        _wantReg.value = true
    }

    fun goneToReg() {
        _wantReg.value = false
    }

}