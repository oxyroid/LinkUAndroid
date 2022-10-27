@file:Suppress("unused")
package com.linku.im.ktx.ui.graphics

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

operator fun Color.times(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

@Composable
fun Color.animated(
    animationSpec: AnimationSpec<Color> = spring(),
    finishedListener: ((Color) -> Unit)? = null
): Color = animateColorAsState(
    targetValue = this,
    animationSpec = animationSpec,
    finishedListener = finishedListener
).value

fun Int.toColor() = Color(this)

fun Long.toColor() = Color(this)
