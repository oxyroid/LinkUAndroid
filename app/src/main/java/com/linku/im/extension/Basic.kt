package com.linku.im.extension

inline fun <R> Boolean?.ifTrue(block: () -> R): R? {
    return if (this == true) block()
    else null
}