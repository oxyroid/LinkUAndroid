package com.linku.im.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp

@Composable
fun ToolBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    title: String = "",
    actions: (@Composable RowScope.() -> Unit)? = null,
    onNavClick: () -> Unit
) {
    Column {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            navigationIcon = {
                MaterialIconButton(icon = navIcon, onClick = onNavClick)
            },
            title = {
                Text(text = title, fontSize = 16.sp)
            },
            actions = actions ?: {}
        )
    }

}