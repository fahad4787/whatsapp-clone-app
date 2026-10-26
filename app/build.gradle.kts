plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.aivora.app"
    compileSdk = 35

    defaultConfig {
        // Different from store/original APK (com.aivora.app) so both can install side-by-side.
        applicationId = "com.aivora.app.dev"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0-dev"
        buildConfigField("String", "API_BASE_URL", "\"https://whatsapp-clone-backend-hep1.onrender.com/\"")
        buildConfigField("String", "WS_BASE_URL", "\"wss://whatsapp-clone-backend-hep1.onrender.com/ws/\"")
        // OTP stays for release / final; debug auto-verifies with 000000 (server OTP_DEV_BYPASS).
        buildConfigField("boolean", "DEV_AUTO_OTP", "false")
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "DEV_AUTO_OTP", "true")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("boolean", "DEV_AUTO_OTP", "false")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    // BoM 33+ folds KTX into the main artifact
    implementation("com.google.firebase:firebase-messaging")

    // Wave 16 — LiveKit Cloud calls
    implementation("io.livekit:livekit-android:2.29.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
