@file:Suppress("UnstableApiUsage")

plugins {
    id(libs.plugins.android.test)
    id(libs.plugins.kotlin.android)
}

android {
    namespace = "com.linku.benchmark"
    compileSdk = 33

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    defaultConfig {
        minSdk = 26
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        // This benchmark buildType is used for benchmarking, and should function like your
        // release build (for example, with minification on). It"s signed with a debug key
        // for easy local/CI testing.
        val benchmark by creating {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks.add("release")
        }
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

dependencies {
    implementation(libs2.androidx.test.ext.junit)
    implementation(libs2.androidx.test.espresso.core)
    implementation(libs2.androidx.test.uiautomator)
    implementation(libs2.androidx.benchmark.macro.junit4)
}

androidComponents {
    beforeVariants {
        it.enable = it.buildType == "benchmark"
    }
}
