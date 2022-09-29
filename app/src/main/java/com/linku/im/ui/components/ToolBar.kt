package com.linku.im.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.linku.im.appyx.target.NavTarget
import com.linku.im.ktx.ifTrue
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
    backStack: BackStack<NavTarget> = LocalBackStack.current,
    onNavClick: () -> Unit = { backStack.pop() },
    actions: @Composable (RowScope.() -> Unit),
    text: String,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    backgroundColor: Color = LocalTheme.current.topBar,
    contentColor: Color = LocalTheme.current.onTopBar
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