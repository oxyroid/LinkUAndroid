package com.linku.domain.extension

import android.graphics.Bitmap
import com.linku.domain.BuildConfig

val <T> T.TAG: String
    get() = "[LinkU]" + this!!::class.java.name


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

fun <R> Bitmap.use(user: (Bitmap) -> R): R = user(this).also { recycle() }