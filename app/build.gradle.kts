plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    kotlin("plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.projectexcursions"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.projectexcursions"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            type = "String",
            name = "MAPKIT_API_KEY",
            value = "\"${project.findProperty("yandexMapKitApiKey")}\"",
        )
        buildConfigField(
            type = "String",
            name = "GHOPPER_API_KEY",
            value = "\"${project.findProperty("graphHopperApiKey")}\"",
        )
        buildConfigField(
            type = "String",
            name = "MAPILARY_API_KEY",
            value = "\"${project.findProperty("mapilaryApiKey")}\"",
        )
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    kapt {
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        correctErrorTypes = true
    }

buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildToolsVersion = "34.0.0"
}

dependencies {

    implementation(libs.glide)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.facebook.shimmer)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.navigation.common.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit2.kotlinx.serialization.converter)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.hilt.android)
    implementation(libs.retrofit)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    implementation(libs.java.jwt)
    implementation(libs.jwtdecode)
    implementation(libs.maps.mobile)
    implementation(libs.play.services.location)
    implementation(libs.androidx.core.splashscreen)
    kapt(libs.androidx.room.compiler)
    kapt(libs.hilt.android.compiler)
    implementation(libs.graphhopper.core)
}