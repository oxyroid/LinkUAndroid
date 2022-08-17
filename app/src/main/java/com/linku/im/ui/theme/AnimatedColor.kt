package com.linku.im.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AnimatedColor(
    val containerColor: Color,
    val onContainerColor: Color,
    val backgroundColor: Color,
    val onBackgroundColor: Color,
    val surfaceColor: Color,
    val onSurfaceColor: Color
)

val LocalAnimatedColor =
    staticCompositionLocalOf<AnimatedColor> { error("No animated color provided") }