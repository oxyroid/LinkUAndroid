package com.wzk.oss.extension

import androidx.compose.ui.graphics.Color

operator fun Color.times(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

inline fun <R> Boolean?.ifTrue(block: () -> R): R? {
    return if (this == true) block()
    else null
}