object libs {

    object google {
        object material {
            val material = "com.google.android.material:material:1.7.0"
        }

        object accompanist {
            private const val version = "0.27.0"

            val insets = "com.google.accompanist:accompanist-insets:$version"
            val `insets-ui` = "com.google.accompanist:accompanist-insets-ui:$version"
            val permissions = "com.google.accompanist:accompanist-permissions:$version"
            val placeholder = "com.google.accompanist:accompanist-placeholder:$version"
            val systemuicontroller =
                "com.google.accompanist:accompanist-systemuicontroller:$version"
            val pager = "com.google.accompanist:accompanist-pager:$version"
            val `pager-indicators` =
                "com.google.accompanist:accompanist-pager-indicators:$version"
        }

        object dagger {
            private const val version = "2.44"
            val agp = "com.google.dagger:hilt-android-gradle-plugin:$version"
            val android = "com.google.dagger:hilt-android:$version"
            val android_compiler = "com.google.dagger:hilt-android-compiler:$version"
        }
    }

    object androidx {
        object core {
            val ktx = "androidx.core:core-ktx:1.9.0"
        }

        val appcompat = "androidx.appcompat:appcompat:1.5.1"

        object activity {
            private const val version = "1.6.1"
            val ktx = "androidx.activity:activity-ktx:$version"
            val compose = "androidx.activity:activity-compose:$version"
        }

        object lifecycle {
            object runtime {
                private const val version = "2.6.0-alpha03"
                val ktx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
                val compose = "androidx.lifecycle:lifecycle-runtime-compose:$version"
            }
        }

        object constraintlayout {
            val compose = "androidx.constraintlayout:constraintlayout-compose:1.0.1"
        }

        object room {
            private const val version = "2.4.3"
            val runtime = "androidx.room:room-runtime:$version"
            val ktx = "androidx.room:room-ktx:$version"
            val compiler = "androidx.room:room-compiler:$version"
        }

        val workmanager = "androidx.work:work-runtime-ktx:2.7.1"
        val recyclerview = "androidx.recyclerview:recyclerview:1.2.1"
        val splashscreen = "androidx.core:core-splashscreen:1.0.0"

        object hilt {
            private const val compiler_version = "1.0.0"
            private const val work_version = "1.0.0"
            private const val navigation_compose_version = "1.0.0"
            val compiler = "androidx.hilt:hilt-compiler:$compiler_version"
            val work = "androidx.hilt:hilt-work:$work_version"
            val navigation = "androidx.hilt:hilt-navigation-compose:$navigation_compose_version"
        }

        object paging {
            val runtime = "androidx.paging:paging-runtime:3.2.0-alpha03"
            val compose = "androidx.paging:paging-compose:1.0.0-alpha17"
        }

        object compose {
            private const val version = "1.4.0-alpha01"

            object ui {
                val ui = "androidx.compose.ui:ui:${version}"
                val `ui-test-junit` = "androidx.compose.ui:ui-test-junit4:${version}"

                object `ui-tooling` {
                    val core = "androidx.compose.ui:ui-tooling:${version}"
                    val preview = "androidx.compose.ui:ui-tooling-preview:${version}"
                }

                object test {
                    val manifest = "androidx.compose.ui:ui-test-manifest:${version}"
                }

            }

            object material {
                val `icons-extended` =
                    "androidx.compose.material:material-icons-extended:${version}"
            }

            val material3 = "androidx.compose.material3:material3:1.1.0-alpha01"

        }
    }

    object test {
        val androidTestExtJunit = "androidx.test.ext:junit:1.1.3"
        val androidTestEspressoCore = "androidx.test.espresso:espresso-core:3.4.0"
        val uiautomator = "androidx.test.uiautomator:uiautomator:2.2.0"
        val benchmarkMacroJunit4 = "androidx.benchmark:benchmark-macro-junit4:1.2.0-alpha06"
    }

    object lottie {
        val compose = "com.airbnb.android:lottie-compose:5.2.0"
    }

    val ucrop = "com.github.yalantis:ucrop:2.2.6"
    val retrofit = "com.squareup.retrofit2:retrofit:2.9.0"
    val appyx = "com.bumble.appyx:core:1.0.0-rc02"

    object coil {
        private const val version = "2.2.2"
        val compose = "io.coil-kt:coil-compose:$version"
        val gif = "io.coil-kt:coil-gif:$version"
    }

    val mmkv = "com.tencent:mmkv:1.2.14"

    object kotlinx {
        object serialzation {
            val core = "org.jetbrains.kotlin:kotlin-serialization:1.7.10"
            val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1"
        }
    }
}
