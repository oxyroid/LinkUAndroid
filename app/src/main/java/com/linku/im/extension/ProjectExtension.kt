package com.linku.im.extension

import com.linku.im.BuildConfig

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