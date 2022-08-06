package com.linku.domain.extension

import kotlinx.serialization.json.Json

val json by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
    Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
}