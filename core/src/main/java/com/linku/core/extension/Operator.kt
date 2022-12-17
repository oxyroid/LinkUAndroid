@file:Suppress("unused")

package com.linku.core.extension

inline fun <R> Boolean?.ifTrue(block: () -> R): R? {
    return if (this == true) block()
    else null
}

inline fun <R> Boolean?.ifFalse(block: () -> R): R? {
    return if (this == false) block()
    else null
}

fun <R> Boolean?.ifTrue(value: R): R? = ifTrue { value }
fun <R> Boolean?.ifFalse(value: R): R? = ifFalse { value }
