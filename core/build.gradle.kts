@file:Suppress("UnstableApiUsage")

plugins {
    id(libs.plugins.android.library)
    id(libs.plugins.kotlin.android)
    id(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.linku.core"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        val debug by getting {
            isMinifyEnabled = false
        }
        val benchmark by creating {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
}

dependencies {
    implementation(libs2.core.ktx)
    implementation(libs2.appcompat)
    implementation(libs2.lifecycle.runtime.compose)
    implementation(libs2.kotlinx.serialization.json)
    implementation(libs2.retrofit)
}
