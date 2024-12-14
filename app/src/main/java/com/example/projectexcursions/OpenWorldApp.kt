package com.example.projectexcursions;

import android.app.Application
import com.yandex.mapkit.MapKitFactory
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

        val MAPKIT_API_KEY = "05f28853-a7b8-468b-83b5-7f909106b088" //не воруйте пж
        MapKitFactory.setApiKey(MAPKIT_API_KEY)
    }
}
