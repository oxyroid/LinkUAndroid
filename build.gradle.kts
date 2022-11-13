@file:Suppress("UnstableApiUsage")
import io.gitlab.arturbosch.detekt.Detekt

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath(libs2.hilt.android.gradle.plugin)
        classpath(libs2.kotlin.serialization)
    }
}

plugins {
    id(libs.plugins.android.application) version libs.plugins.agp_version apply false
    id(libs.plugins.android.library) version libs.plugins.agp_version apply false
    id(libs.plugins.android.test) version libs.plugins.agp_version apply false
    id(libs.plugins.kotlin.android) version libs.plugins.kotlin_version apply false
    id(libs.plugins.detekt.detekt) version libs.plugins.detekt_version apply true
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
