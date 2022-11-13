object libs {
    object plugins {
        const val agp_version = "7.3.1"
        const val kotlin_version = "1.7.10"
        const val detekt_version = "1.22.0-RC3"

        object android {
            const val application = "com.android.application"
            const val library = "com.android.library"
            const val test = "com.android.test"
        }
        object kotlin {
            const val android = "org.jetbrains.kotlin.android"
            const val kapt = "kotlin-kapt"
            const val parcelize = "kotlin-parcelize"
        }
        object kotlinx {
            const val serialization = "kotlinx-serialization"
        }
        object detekt {
            const val detekt = "io.gitlab.arturbosch.detekt"
        }
        object hilt {
            const val android = "dagger.hilt.android.plugin"
        }
    }
}
