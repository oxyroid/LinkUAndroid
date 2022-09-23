package com.linku.im.extension.compose.core

import androidx.compose.ui.graphics.Color


operator fun Color.times(alpha: Float): Color {
    return this.copy(alpha = alpha)
}
