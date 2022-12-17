package com.linku.im.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.linku.im.nav.target.NavTarget
import com.linku.im.ktx.runtime.rememberedRun
import com.linku.im.ktx.ui.graphics.animated
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MaterialTopBar(
    text: String,
    actions: @Composable (RowScope.() -> Unit),
    modifier: Modifier = Modifier,
    backStack: BackStack<NavTarget> = LocalBackStack.current,
    onNavClick: () -> Unit = { backStack.pop() },
    navIcon: ImageVector = Icons.Default.ArrowBack,
    backgroundColor: Color = LocalTheme.current.topBar.animated(),
    contentColor: Color = LocalTheme.current.onTopBar.animated()
) {
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Column(modifier.background(backgroundColor)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            TopAppBar(
                title = {
                    val duration = if (text.isNotEmpty()) MaterialTopBarDefaults.EnabledDuration
                    else MaterialTopBarDefaults.DisabledDuration
                    Row {
                        val animation = rememberedRun(duration) {
                            slideInVertically(tween(this)) { it } +
                                    fadeIn(tween(this)) with
                                    slideOutVertically(tween(this)) { -it } +
                                    fadeOut(tween(this))
                        }
                        AnimatedContent(targetState = text, transitionSpec = {
                            animation.using(
                                SizeTransform(true)
                            )
                        }) { target ->
                            Text(
                                text = target,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = null
                                ),
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

private object MaterialTopBarDefaults {
    const val EnabledDuration = 800
    const val DisabledDuration = 0
}
