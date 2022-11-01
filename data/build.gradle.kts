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
    namespace = "com.linku.data"
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
        val apiKey: String = localProperties.getProperty("API_KEY", "")
        val storageBaseUrl: String = localProperties.getProperty("STORAGE_PRE", "")

        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "API_KEY", "\"$apiKey\"")
        buildConfigField("String", "STORAGE_PRE", "\"$storageBaseUrl\"")
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
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.appcompat:appcompat:$appcompatVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$androidTestExtJunitVersion")
    androidTestImplementation("androidx.test.espresso:espresso-core:$androidTestEspressoCoreVersion")

    // room
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

    // mmkv
    implementation("com.tencent:mmkv:$mmkvVersion")

    // paging
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingComposeVersion")
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltCompilerVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltNavigationVersion")

}
