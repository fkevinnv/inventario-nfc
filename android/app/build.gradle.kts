plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Dirección del servidor. Por defecto "localhost" (se usa con: adb reverse tcp:8080 tcp:8080).
// Para usar la IP del PC en la wifi: gradlew assembleDebug -PurlBase=http://192.168.1.50:8080/
val urlBase: String = (project.findProperty("urlBase") as String?) ?: "http://localhost:8080/"

android {
    namespace = "com.inventarionfc.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.inventarionfc.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Se guarda en BuildConfig.URL_BASE para leerlo desde el código
        buildConfigField("String", "URL_BASE", "\"$urlBase\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Cliente HTTP y conversión de JSON
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
