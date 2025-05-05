package com.example.projectexcursions.modules

import android.app.Application
import com.example.projectexcursions.OpenWorldApp
import com.example.projectexcursions.R
import com.example.projectexcursions.net.ApiService
import com.example.projectexcursions.repositories.tokenrepo.TokenRepository
import javax.net.ssl.X509TrustManager
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
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://217.71.129.139:5383/api/v1/"

    @Provides
    @Singleton
    fun provideSecureOkHttpClient(
        tokenRepo: TokenRepository,
        app: Application
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val certificateFactory = CertificateFactory.getInstance("X.509")
        val inputStream = app.resources.openRawResource(R.raw.server)
        val certificate = certificateFactory.generateCertificate(inputStream)
        inputStream.close()

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
            setCertificateEntry("ca", certificate)
        }

        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        ).apply {
            init(keyStore)
        }

        val x509TrustManager = trustManagerFactory.trustManagers[0] as X509TrustManager

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, arrayOf(x509TrustManager), null)
        }

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, x509TrustManager)
            .hostnameVerifier(myHostNameVerifier())
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val token = runBlocking { tokenRepo.getToken()?.token }
                val requestBuilder = chain.request().newBuilder()
                if (token != null) {
                    requestBuilder.addHeader("Authorization", token.toString())
                }
                chain.proceed(requestBuilder.build())
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideDefaultOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
    }

    @Singleton
    @Provides
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

    private fun myHostNameVerifier(): HostnameVerifier {
        return HostnameVerifier { hostname, _ -> hostname == "217.71.129.139" }
    }
}
