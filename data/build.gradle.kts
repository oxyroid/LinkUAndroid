@file:Suppress("UnstableApiUsage")

import dsl.localProperties

plugins {
    id(libs.plugins.android.library)
    id(libs.plugins.kotlin.android)
    id(libs.plugins.hilt.android)
    id(libs.plugins.kotlinx.serialization)
    id(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.linku.data"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        val baseUrl: String = localProperties.getProperty("base_url", "")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
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
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs2.androidx.core.ktx)
    implementation(libs2.androidx.lifecycle.runtime.ktx)
    implementation(libs2.androidx.appcompat)

    implementation(libs2.bundles.androidx.room)
    kapt(libs2.androidx.room.compiler)

    implementation(libs2.kotlinx.serialization.json)
    implementation(libs2.squareup.retrofit)

    implementation(libs2.tencent.mmkv)

    implementation(libs2.bundles.androidx.paging)

    kapt(libs2.bundles.hilt.kapt)
    implementation(libs2.bundles.hilt.implementation)
}
