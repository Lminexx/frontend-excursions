package com.example.projectexcursions

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenWorldApp: Application() {

    companion object {
        private var instance: OpenWorldApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
    }
}