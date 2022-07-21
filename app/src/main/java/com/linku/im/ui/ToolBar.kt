package com.linku.im.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.linku.im.vm

@Composable
fun ToolBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    title: String = "",
    onScroll: Boolean = false,
    isDarkMode: Boolean = false,
    onNavClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    val elevation by animateDpAsState(if (onScroll) 16.dp else 0.dp)
    val color = if (!isDarkMode) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surface
    val contentColor = if (!isDarkMode) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurface
    Column {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(color)
        )
        TopAppBar(
            backgroundColor = color,
            contentColor = contentColor,
            navigationIcon = {
                MaterialIconButton(
                    icon = navIcon,
                    onClick = onNavClick,
                    tint = contentColor
                )
            },
            title = {
                Text(
                    text = title,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            actions = actions,
            elevation = elevation
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
            tint = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary
        )
    }
}