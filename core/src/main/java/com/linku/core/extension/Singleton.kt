package com.linku.core.extension

import kotlinx.serialization.json.Json

val json by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}