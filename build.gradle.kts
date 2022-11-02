buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs.google.dagger.agp)
        classpath(libs.kotlinx.serialzation.core)
    }
}

plugins {
    id("com.android.application") version "7.3.1" apply false
    id("com.android.library") version "7.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.7.10" apply false
    id("com.android.test") version "7.3.1" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
