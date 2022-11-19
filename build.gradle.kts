@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.Detekt

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs2.dagger.hilt.android.gradle.plugin)
        classpath(libs2.kotlin.serialization)
    }
}

plugins {
    val kotlinVersion = "1.7.20"
    val agpVersion = "7.3.1"
    val detektVersion = "1.22.0-RC3"
    id(libs.plugins.android.application) version agpVersion apply false
    id(libs.plugins.android.library) version agpVersion apply false
    id(libs.plugins.android.test) version agpVersion apply false
    id(libs.plugins.kotlin.android) version kotlinVersion apply false
    id(libs.plugins.detekt.detekt) version detektVersion apply true
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
