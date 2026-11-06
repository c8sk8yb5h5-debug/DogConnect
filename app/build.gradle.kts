plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val releaseRun = providers.gradleProperty("dogconnect.releaseRun").orNull?.toIntOrNull()

android {
    namespace = "com.c8sk8yb5h5.dogconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.c8sk8yb5h5.dogconnect"
        minSdk = 26
        targetSdk = 35
        versionCode = if (releaseRun != null) 2000 + releaseRun else 2
        versionName = if (releaseRun != null) "0.2.$releaseRun" else "0.2.0"
    }

    buildFeatures { buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
}
