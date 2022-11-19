@file:Suppress("UnstableApiUsage")

plugins {
    id(libs.plugins.android.application)
    id(libs.plugins.kotlin.android)
    id(libs.plugins.kotlin.kapt)
    id(libs.plugins.kotlin.parcelize)
    id(libs.plugins.kotlinx.serialization)
    id(libs.plugins.hilt.android)
    id(libs.plugins.detekt.detekt)
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn", "-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    androidResources {
        noCompress += "txt"
    }
    configurations {
        implementation.get().exclude(group = "com.intellij", module = "annotations")
    }
}

detekt {
    source = files("src/main/java")
    config = files("$rootDir/config/detekt.yml")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(libs2.androidx.core.ktx)
    implementation(libs2.androidx.activity.ktx)
    implementation(libs2.androidx.activity.compose)
    implementation(libs2.androidx.lifecycle.runtime.compose)
    implementation(libs2.androidx.lifecycle.runtime.ktx)
    implementation(libs2.androidx.recyclerview)
    implementation(libs2.androidx.core.splashscreen)
    implementation(libs2.androidx.constraintlayout.compose)
    implementation(libs2.material)

    implementation(libs2.androidx.compose.ui.asProvider())
    implementation(libs2.androidx.compose.ui.test.manifest)
    implementation(libs2.androidx.compose.ui.tooling.core)
    implementation(libs2.androidx.compose.ui.tooling.preview)
    implementation(libs2.androidx.compose.material.icons.extended)
    implementation(libs2.androidx.compose.material3)

    kapt(libs2.bundles.hilt.kapt)
    implementation(libs2.bundles.hilt.implementation)

    implementation(libs2.androidx.work.runtime.ktx)

    implementation(libs2.bundles.androidx.paging)

    implementation(libs2.androidx.startup.runtime)

    implementation(libs2.bundles.accompanist)

    implementation(libs2.kotlinx.serialization.json)

    implementation(libs2.bundles.coil)

    implementation(libs2.airbnb.android.lottie.compose)
    implementation(libs2.squareup.retrofit)
    implementation(libs2.bumble.appyx.core)
    implementation(libs2.yalantis.ucrop)
    implementation(libs2.tencent.mmkv)

    detektPlugins(libs2.twitter.compose.rules.detekt)
}
