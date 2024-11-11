package com.example.projectexcursions.user

import android.text.TextUtils
import android.util.Patterns
import androidx.databinding.BaseObservable

class User(private var login: String, private var password: String):BaseObservable() {
    val isDataValid: Boolean
        get() = (!TextUtils.isEmpty(getLogin())) && getPasword().length > 3

    fun getPasword(): String {
        return password
    }

    fun getLogin(): String {
        return login
    }

    fun setLogin(login: String) {
        this.login = login
    }

    fun setPass(password: String) {
        this.password = password
    }
}