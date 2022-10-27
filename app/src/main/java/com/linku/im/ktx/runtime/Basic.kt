package com.linku.im.ktx.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls


@Composable
inline fun <T, R> rememberedRun(
    key: T,
    crossinline calculation: @DisallowComposableCalls T.() -> R
): R = androidx.compose.runtime.remember(key) { calculation(key) }
