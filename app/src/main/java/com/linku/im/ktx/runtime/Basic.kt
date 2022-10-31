package com.linku.im.ktx.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember


@Composable
inline fun <T, R> rememberedRun(
    key: T,
    crossinline calculation: @DisallowComposableCalls T.() -> R
): R = remember(key) { calculation(key) }
