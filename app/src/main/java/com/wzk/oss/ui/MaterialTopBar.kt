package com.wzk.oss.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MaterialTopBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    title: String = "",
    actions: (@Composable RowScope.() -> Unit)? = null,
    onNavClick: () -> Unit
) {
    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        navigationIcon = {
            MaterialIconButton(icon = navIcon, onClick = onNavClick)
        },
        title = {
            Text(text = title)
        },
        actions = actions ?: {}
    )
}