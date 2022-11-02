@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
}
android {
    namespace = "com.linku.im"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.linku.im"
        minSdk = 26
        targetSdk = 33
        versionCode = configs.versionCode
        versionName = configs.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        val debug by getting {
            isMinifyEnabled = false
            isDebuggable = true
        }
        val benchmark by creating {
            isDebuggable = false
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        noCompress += "txt"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.constraintlayout.compose)

    implementation(libs.google.material.material)

    implementation(libs.androidx.compose.ui.ui)
    androidTestImplementation(libs.androidx.compose.ui.`ui-test-junit`)
    debugImplementation(libs.androidx.compose.ui.`ui-tooling`.core)
    debugImplementation(libs.androidx.compose.ui.`ui-tooling`.preview)
    implementation(libs.androidx.compose.material.`icons-extended`)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.test.manifest)


    kapt(libs.androidx.hilt.compiler)
    kapt(libs.google.dagger.android_compiler)
    implementation(libs.google.dagger.android)
    implementation(libs.androidx.hilt.navigation)
    implementation(libs.androidx.hilt.work)

    implementation(libs.androidx.workmanager)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.google.accompanist.insets)
    implementation(libs.google.accompanist.`insets-ui`)
    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.accompanist.placeholder)
    implementation(libs.google.accompanist.systemuicontroller)
    implementation(libs.google.accompanist.pager)
    implementation(libs.google.accompanist.`pager-indicators`)

    implementation(libs.kotlinx.serialzation.json)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    implementation(libs.lottie.compose)
    implementation(libs.retrofit)
    implementation(libs.appyx)
    implementation(libs.ucrop)
    implementation(libs.mmkv)
}
