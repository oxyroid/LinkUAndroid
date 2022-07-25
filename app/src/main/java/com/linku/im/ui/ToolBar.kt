package com.linku.im.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun ToolBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    title: @Composable () -> Unit,
    onNavClick: () -> Unit,
    isDarkMode: Boolean,
    actions: @Composable RowScope.() -> Unit
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
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
        )
        TopAppBar(
            title = title,
            backgroundColor = toolbarColor,
            contentColor = onToolbarColor,
            elevation = 0.dp,
            navigationIcon = {
                MaterialIconButton(icon = navIcon, onClick = onNavClick, tint = onToolbarColor)
            },
            actions = actions
        )
    }
}

@Composable
fun ToolBarAction(
    onClick: () -> Unit,
    imageVector: ImageVector,
    tint: Color,
    contentDescription: String = imageVector.name
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}