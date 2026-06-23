import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

// ── Load keystore.properties (never committed to git) ──────────────────────
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) load(keystorePropsFile.inputStream())
}

android {
    namespace  = "com.kiladarbar"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kiladarbar"
        minSdk        = 26
        targetSdk     = 36
        versionCode   = 1
        versionName   = "1.0.0"
    }

    // ── Signing ────────────────────────────────────────────────────────────
    signingConfigs {
        create("release") {
            storeFile     = file(keystoreProps["storeFile"] as? String ?: "kila-darbar-release.jks")
            storePassword = keystoreProps["storePassword"] as? String ?: "KilaDarbar@2025"
            keyAlias      = keystoreProps["keyAlias"]      as? String ?: "kila-darbar"
            keyPassword   = keystoreProps["keyPassword"]   as? String ?: "KilaDarbar@2025"
        }
    }

    // ── Build types ────────────────────────────────────────────────────────
    buildTypes {
        debug {
            // Targets (set DEV_HOST env var or change the default):
            //   Emulator              → 10.0.2.2          (no setup needed)
            //   Real device USB       → 10.0.2.2 + run:  adb reverse tcp:8080 tcp:8080
            //   Real device WiFi      → your LAN IP       (sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT)
            val devHost = System.getenv("DEV_HOST") ?: "192.168.68.140"
            buildConfigField("String", "BASE_URL", "\"http://$devHost:8080/api/\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"26870838602-0mh3bc921cc3rfdeabb7qs91o5f37qi8.apps.googleusercontent.com\"")
        }

        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            signingConfig     = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.kiladarbar.com/api/\"")
            buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"YOUR_PROD_WEB_CLIENT_ID.apps.googleusercontent.com\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")

    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Google Auth / Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")

    // Lottie animations
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
