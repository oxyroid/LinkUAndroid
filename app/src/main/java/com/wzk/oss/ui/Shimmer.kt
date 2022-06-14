package com.wzk.oss.ui

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun shimmerBrush(baseColor: Color = Color.LightGray) = run {
    val shimmerColors = listOf(
        baseColor.copy(alpha = 0.9f),
        baseColor.copy(alpha = 0.2f),
        baseColor.copy(alpha = 0.9f)
    )
    val translation = rememberInfiniteTransition()
    val translateAnimation = translation.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = 1200,
                easing = FastOutLinearInEasing
            ),
            RepeatMode.Restart
        )
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnimation.value, translateAnimation.value)
    )
    brush
}