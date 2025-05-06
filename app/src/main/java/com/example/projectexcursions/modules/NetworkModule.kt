package com.example.projectexcursions.modules

import android.app.Application
import com.example.projectexcursions.R
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.*

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://217.71.129.139:5383/api/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenRepo: TokenRepository,
        app: Application
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val trustManager = createUnifiedTrustManager(app)
        val sslContext = SSLContext.getInstance("TLS").apply {
            init(null, arrayOf<TrustManager>(trustManager), null)
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true } // Подумай об усилении позже
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val token = runBlocking { tokenRepo.getToken()?.token }
                val request = chain.request().newBuilder().apply {
                    if (token != null) addHeader("Authorization", token.toString())
                }.build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createUnifiedTrustManager(app: Application): X509TrustManager {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream = app.resources.openRawResource(R.raw.server)
        val customCert = certificateFactory.generateCertificate(inputStream)
        inputStream.close()

        val customKeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("custom", customCert)
        }

        val customTmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(customKeyStore)
        }
        val customTm = customTmFactory.trustManagers.filterIsInstance<X509TrustManager>().first()

        val defaultTmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply {
            init(null as KeyStore?)
        }
        val defaultTm = defaultTmFactory.trustManagers.filterIsInstance<X509TrustManager>().first()

        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                try {
                    defaultTm.checkClientTrusted(chain, authType)
                } catch (e: Exception) {
                    customTm.checkClientTrusted(chain, authType)
                }
            }

            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                try {
                    defaultTm.checkServerTrusted(chain, authType)
                } catch (e: Exception) {
                    customTm.checkServerTrusted(chain, authType)
                }
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return defaultTm.acceptedIssuers + customTm.acceptedIssuers
            }
        }
    }

    @Provides
    @Singleton
    @OptIn(ExperimentalSerializationApi::class)
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}