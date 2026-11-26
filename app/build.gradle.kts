import java.util.Properties
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
        versionName = "1.0.0"
        buildConfigField("String", "API_BASE_URL", "\"https://whatsapp-clone-backend-hep1.onrender.com/\"")
        buildConfigField("String", "WS_BASE_URL", "\"wss://whatsapp-clone-backend-hep1.onrender.com/ws/\"")
        // Wave 23: arm64-only sideload (Mate/Vivo). Dropping armeabi-v7a saves ~8MB WebRTC.
        // Emulator x86 / 32-bit: local override — abiFilters += "x86_64" or "armeabi-v7a".
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    // Release signing: create keystore.properties (gitignored) with storeFile, storePassword,
    // keyAlias, keyPassword. Without it, release falls back to the debug key (local testing only).
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }
    val hasReleaseKey = keystoreProps.getProperty("storeFile") != null
    // v4 signature (.idsig) lets scripts/dev-install.sh use `adb install --incremental`:
    // the app starts while the rest of the APK streams in (Android 11+).
    signingConfigs.getByName("debug") { enableV4Signing = true }
    if (hasReleaseKey) {
        signingConfigs.create("release") {
            storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }

    buildTypes {
        debug {
            // Keep debug unminified — R8 debug slowed iteration; release still minifies.
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName(if (hasReleaseKey) "release" else "debug")
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
    lint {
        // ComponentActivity + Activity Result; lint falsely flags when Fragment is transitive-old.
        disable += "InvalidFragmentVersionForActivityResult"
    }
}

// Opt-in: ./gradlew :app:compileDebugKotlin -PcomposeCompilerReports=true
// → app/build/compose_compiler/ (skippable / restartable reports for H1 cuts)
composeCompiler {
    if (project.findProperty("composeCompilerReports") == "true") {
        metricsDestination.set(layout.buildDirectory.dir("compose_compiler"))
        reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // Wave 23: extended kept — material-icons-core lacks Mic/Videocam/etc. (~50 glyphs).
    // Release R8 strips unused; debug DEX cost documented in WAVE23_APK_SIZE.md.
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Wave 33 — app lock with device biometric / credential
    implementation("androidx.biometric:biometric:1.1.0")
    // Wave 38/44 — profile QR encode + gallery/live decode
    implementation("com.google.zxing:core:3.5.3")
    val cameraX = "1.4.2"
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
    // F13 — WhatsApp-style native recycle on hot lists (Mate-class fling).
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Wave 48 — scheduled chat backup
    implementation("androidx.work:work-runtime-ktx:2.9.1")

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

    implementation(platform("com.google.firebase:firebase-bom:35.1.0"))
    // BoM 33+ folds KTX into the main artifact
    implementation("com.google.firebase:firebase-messaging")

    // Wave 16 — LiveKit Cloud calls
    implementation("io.livekit:livekit-android:2.29.1")

    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
