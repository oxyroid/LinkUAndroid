package com.linku.im.ktx.compose.ui.graphics

import androidx.compose.ui.graphics.Color

operator fun Color.times(alpha: Float): Color {
    return this.copy(alpha = alpha)
}