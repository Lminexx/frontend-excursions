package com.example.projectexcursions

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.exlistrepo.ExcursionRepository
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltAndroidApp
class OpenWorldApp : Application() {

    @Inject
    lateinit var tokenRepository: TokenRepository

    @Inject
    lateinit var apiService: ApiService

    @Inject
    lateinit var excRepository: ExcursionRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            excRepository.deleteAllExcursionsFromExcursion()
        }

        FirebaseApp.initializeApp(this)
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        Log.d("YandexMap", "API Key: ${BuildConfig.MAPKIT_API_KEY}")

        applicationScope.launch {
            try {
                tokenRepository.validateToken(apiService)
                Toast.makeText(applicationContext, R.string.enjoy_using_it, Toast.LENGTH_SHORT)
                    .show()
            } catch (http: HttpException) {
                when (http.code()) {
                    401 -> {

                        if (tokenRepository.getToken() != null) {
                            tokenRepository.deleteToken(tokenRepository.getCachedToken()!!.token)
                            Toast.makeText(
                                applicationContext,
                                R.string.please_register,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    else -> Toast.makeText(applicationContext, http.message, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message ?: "Unknown error", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}