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
        versionCode = versionCodeConfig
        versionName = versionNameConfig

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        val release by getting {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        val benchmark by creating {
            signingConfig = signingConfigs.getByName("debug")
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
        noCompress += "config"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation("androidx.core:core-ktx:$coreKtxVersion")
    implementation("androidx.activity:activity-ktx:$activityVersion")
    implementation("androidx.activity:activity-compose:$activityVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.core:core-splashscreen:$splashscreenVersion")
    implementation("androidx.recyclerview:recyclerview:$recyclerviewVersion")
    testImplementation("junit:junit:$junitVersion")
    androidTestImplementation("androidx.test.ext:junit:$androidTestExtJunitVersion")
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material3:material3:$composeMaterial3Version")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // constraint Layout
    implementation("androidx.constraintlayout:constraintlayout-compose:$constraintVersion")

    // hilt
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    kapt("androidx.hilt:hilt-compiler:$hiltCompilerVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltNavigationVersion")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // coil
    implementation("io.coil-kt:coil-compose:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")

    // lottie
    implementation("com.airbnb.android:lottie-compose:$lottieComposeVersion")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")

    // paging
    implementation("androidx.paging:paging-runtime:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingComposeVersion")

    // accompanist
    implementation("com.google.accompanist:accompanist-insets:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-insets-ui:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-placeholder:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-pager-indicators:$accompanistVersion")

    // json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")

    // appyx
    implementation("com.bumble.appyx:core:$appyxVersion")

    // uCrop
    implementation("com.github.yalantis:ucrop:$ucropVersion")

    // mmkv
    implementation("com.tencent:mmkv:$mmkvVersion")

}
