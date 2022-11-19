object libs {
    object plugins {

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
