package com.linku.im.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.linku.im.extension.ifTrue
import com.linku.im.vm

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ToolBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: () -> Unit,
    actions: @Composable (RowScope.() -> Unit),
    text: String,
    backgroundColor: Color = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
    contentColor: Color = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
) {
    Column(Modifier.background(backgroundColor)) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        SmallTopAppBar(
            title = {
                val duration = text.isNotEmpty().ifTrue { 800 } ?: 0
                Row {
                    Spacer(Modifier.width(8.dp))
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
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            },
            colors = TopAppBarDefaults.smallTopAppBarColors(
                containerColor = backgroundColor,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            ),
            navigationIcon = {
                MaterialIconButton(
                    icon = navIcon,
                    onClick = onNavClick,
                )
            },
            actions = actions
        )
    }
}

@Composable
fun ToolBarAction(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String = imageVector.name
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription
        )
    }
}