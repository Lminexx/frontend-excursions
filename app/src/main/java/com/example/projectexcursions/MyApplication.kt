package com.example.projectexcursions;

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        private var instance: MyApplication? = null

        fun getInstance(): MyApplication {
            return instance ?: throw IllegalStateException("MyApplication is not initialized")
        }

        fun getAuthToken(): String? {
        return getInstance().getSharedPreferences("auth_prefs", MODE_PRIVATE)
                .getString("auth_token", null)
        }


        fun saveAuthToken(token: String) {
            getInstance().getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit().putString("auth_token", token).apply()
        }

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
