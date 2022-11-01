buildscript {
    repositories {
        google()
        gradlePluginPortal()
    }

    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
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
