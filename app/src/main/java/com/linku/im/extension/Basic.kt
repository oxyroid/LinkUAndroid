package com.linku.im.extension

inline fun <R> Boolean?.ifTrue(block: () -> R): R? {
    return if (this == true) block()
    else null
}

inline fun <R> Boolean?.ifFalse(block: () -> R): R? {
    return if (this == false) block()
    else null
}