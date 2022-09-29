@file:Suppress("unused")

package com.linku.im.ktx

import android.content.Context
import android.widget.Toast

inline fun <R> Boolean?.ifTrue(block: () -> R): R? {
    return if (this == true) block()
    else null
}

inline fun <R> Boolean?.ifFalse(block: () -> R): R? {
    return if (this == false) block()
    else null
}

fun Context.toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, text, duration).show()
}

inline fun <K, V> MutableMap<K, V>.getOrPutNullable(k: K, block: () -> V?): V? =
    get(k) ?: block()?.also { put(k, it) }
