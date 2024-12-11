package com.example.projectexcursions;

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenWorldApp : Application() {

    companion object {
        private var instance: OpenWorldApp? = null

        fun getInstance(): OpenWorldApp {
            return instance ?: throw IllegalStateException("OpenWorldApp is not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
