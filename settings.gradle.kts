@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
    versionCatalogs {
        create("libs2") {
            from(files("gradle/libs2.versions.toml"))
        }
    }
}
rootProject.name = "LinkU-Android"
include(":app", ":core", ":domain", ":data", ":benchmark")
enableFeaturePreview("VERSION_CATALOGS")
