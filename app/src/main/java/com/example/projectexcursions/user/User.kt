package com.example.projectexcursions.user

import android.text.TextUtils

class User(private var login: String, private var password: String){

    var isDataValid: Boolean = false

    fun getPassword(): String {
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