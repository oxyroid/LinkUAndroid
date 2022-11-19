package com.linku.im.ui.brush

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
@Stable
fun premiumBrush(
    color1: Color = Color(0xff897fee),
    color2: Color = Color(0xffd859a9)
): Brush {
    val transition = rememberInfiniteTransition()

    val leftColor by transition.animateColor(
        initialValue = color1,
        targetValue = color2,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val rightColor by transition.animateColor(
        initialValue = color2,
        targetValue = color1,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    return Brush.linearGradient(
        colors = listOf(leftColor, rightColor)
    )
}
