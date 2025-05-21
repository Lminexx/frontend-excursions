package com.example.projectexcursions.modules

import android.annotation.SuppressLint
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

@GlideModule
class CustomGlideModule : AppGlideModule() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface GlideOkHttpClientEntryPoint {
        fun okHttpClientBuilder(): OkHttpClient.Builder
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            GlideOkHttpClientEntryPoint::class.java
        )

        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(
                entryPoint.okHttpClientBuilder()
                    .disableCertPathValidation()
                    .build()
            )
        )
    }
    
    @SuppressLint("TrulyRandom")
    fun OkHttpClient.Builder.disableCertPathValidation(): OkHttpClient.Builder {
        val sslContext = SSLContext.getInstance("SSL")
        val trustManager = DisabledTrustManager()
        sslContext.init(null, arrayOf(trustManager), SecureRandom())
        sslSocketFactory(sslContext.socketFactory, trustManager)
        return hostnameVerifier { _, _ -> true }
    }

    @SuppressLint("TrustAllX509TrustManager,CustomX509TrustManager")
    private class DisabledTrustManager : X509TrustManager {

        override fun getAcceptedIssuers(): Array<out X509Certificate> = arrayOf()

        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
            // empty
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
            // empty
        }
    }
}
