package com.linku.data

import com.linku.domain.BuildConfig

val <T> T.TAG: String
    get() = "[LinkU]" + this!!::class.java.simpleName

inline fun <R> debug(block: () -> R): R? {
    return try {
        if (BuildConfig.DEBUG) block()
        else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

inline fun <R> release(block: () -> R): R? {
    return try {
        if (!BuildConfig.DEBUG) block()
        else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}