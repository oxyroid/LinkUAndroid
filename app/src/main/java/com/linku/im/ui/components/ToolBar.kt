package com.linku.im.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linku.im.ktx.ifTrue
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ToolBar(
    onNavClick: () -> Unit,
    actions: @Composable (RowScope.() -> Unit),
    text: String,
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    backgroundColor: Color = LocalTheme.current.surface,
    contentColor: Color = LocalTheme.current.onSurface,
) {
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Column(modifier.background(backgroundColor)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            TopAppBar(
                title = {
                    val duration = text.isNotEmpty().ifTrue { 800 } ?: 0
                    Row {
                        val animation = remember {
                            slideInVertically(tween(duration)) { it } +
                                    fadeIn(tween(duration)) with
                                    slideOutVertically(tween(duration)) { -it } +
                                    fadeOut(tween(duration))
                        }
                        AnimatedContent(
                            targetState = text,
                            transitionSpec = {
                                animation.using(
                                    SizeTransform(true)
                                )
                            }
                        ) { target ->
                            Text(
                                text = target,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                },
                navigationIcon = {
                    MaterialIconButton(
                        icon = navIcon,
                        onClick = onNavClick,
                    )
                },
                actions = actions,
                backgroundColor = backgroundColor,
                contentColor = LocalContentColor.current,
                elevation = 0.dp
            )
        }
    }
}