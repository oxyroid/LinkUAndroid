package dsl

import org.gradle.api.Project
import java.util.Properties

inline val Project.localProperties: Properties
    get() = Properties().apply {
        val file = project.rootProject.file("local.properties")
        if (!file.exists()) file.createNewFile()
        load(file.inputStream())
    }
