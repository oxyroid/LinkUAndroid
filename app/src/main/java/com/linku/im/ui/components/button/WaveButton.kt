package com.linku.im.ui.components.button

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import com.linku.im.ktx.compose.ui.graphics.times

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WaveIconButton(
    active: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    contentDescription: String? = null,
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = active,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val float by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            val rotation1 by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800),
                    repeatMode = RepeatMode.Reverse
                )
            )
            val rotation2 by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1600),
                    repeatMode = RepeatMode.Reverse
                )
            )
            val rotation3 by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Canvas(
                modifier = modifier,
                onDraw = {
                    val radius = size.minDimension / 2.0f
                    rotate(rotation1 * 360) {
                        drawCircle(
                            color = backgroundColor * 0.6f,
                            center = this.center + Offset(
                                x = float * radius,
                                y = float * radius
                            )
                        )
                    }
                }
            )
            Canvas(
                modifier = modifier,
                onDraw = {
                    val radius = size.minDimension / 2.0f
                    rotate(rotation2 * 360) {
                        drawCircle(
                            color = backgroundColor * 0.6f,
                            center = this.center + Offset(
                                x = 0f,
                                y = float * radius
                            )
                        )
                    }

                }
            )
            Canvas(
                modifier = modifier,
                onDraw = {
                    val radius = size.minDimension / 2.0f
                    rotate(rotation2 * 360) {
                        drawCircle(
                            color = backgroundColor * 0.6f,
                            center = this.center + Offset(
                                x = float * radius,
                                y = 0f
                            )
                        )
                    }
                }
            )
        }
        FilledIconButton(
            onClick,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = backgroundColor,
                contentColor = contentColor
            )
        ) {
            Crossfade(icon) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor
                )
            }
        }
    }
}