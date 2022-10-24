@file:Suppress("unused")

package com.linku.im.ktx

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls

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

@Composable
inline fun <T, R> rememberedRun(
    key: T,
    crossinline calculation: @DisallowComposableCalls T.() -> R
): R =
    androidx.compose.runtime.remember(key) { calculation(key) }
