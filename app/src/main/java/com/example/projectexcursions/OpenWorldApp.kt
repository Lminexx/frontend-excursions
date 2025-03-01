package com.example.projectexcursions

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
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

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()

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
                        if (tokenRepository.getToken()!=null) {
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