package com.linku.im.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ToolBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    onNavClick: () -> Unit,
    isDarkMode: Boolean,
    actions: @Composable (RowScope.() -> Unit),
    title: @Composable () -> Unit,
) {
    val toolbarColor =
        if (isDarkMode) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.primary

    val onToolbarColor =
        if (isDarkMode) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onPrimary

    Column(
        modifier = Modifier.background(toolbarColor)
    ) {
        Spacer(
            modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars)
        )
        SmallTopAppBar(
            title = {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    title()
                }
            },
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = toolbarColor,
                titleContentColor = onToolbarColor,
                navigationIconContentColor = onToolbarColor,
                actionIconContentColor = onToolbarColor
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
            contentDescription = contentDescription,
        )
    }
}