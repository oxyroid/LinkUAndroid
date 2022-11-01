@file:Suppress("UnstableApiUsage")

import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlinx-serialization")
}

android {
    namespace = "com.linku.domain"
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) file.inputStream().use(::load)
        }
        val baseUrl: String = localProperties.getProperty("BASE_URL", "")
        val wsUrl: String = localProperties.getProperty("WS_URL", "")

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "WS_URL", "\"$wsUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
}

dependencies {
    implementation(project(":core"))

    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltNavigationVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltCompilerVersion")

    // room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")

    // paging
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingComposeVersion")

}
