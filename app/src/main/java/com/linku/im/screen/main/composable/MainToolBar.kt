package com.linku.im.screen.main.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.linku.im.nav.target.NavTarget
import com.linku.im.ktx.ui.graphics.animated
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalTheme

@Composable
fun MainToolBar(
    actions: @Composable (RowScope.() -> Unit),
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
    backStack: BackStack<NavTarget> = LocalBackStack.current,
    onNavClick: () -> Unit = { backStack.pop() },
    backgroundColor: Color = LocalTheme.current.topBar.animated(),
    contentColor: Color = LocalTheme.current.onTopBar.animated()
) {
    CompositionLocalProvider(
        LocalContentColor provides contentColor
    ) {
        Column(modifier.background(backgroundColor)) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            TopAppBar(
                title = text,
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
