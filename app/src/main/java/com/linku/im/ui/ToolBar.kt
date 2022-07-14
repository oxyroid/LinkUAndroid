package com.linku.im.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.im.ui.theme.Typography

@Composable
fun ToolBar(
    navIcon: ImageVector = Icons.Default.ArrowBack,
    title: String = "",
    onScroll: Boolean = false,
    onMenuClick: () -> Unit,
    onNavClick: () -> Unit
) {
    val elevation by animateDpAsState(if (onScroll) 16.dp else 4.dp)
    Column {
        Spacer(
            modifier = Modifier
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(MaterialTheme.colorScheme.surface)
        )
        TopAppBar(
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            navigationIcon = {
                MaterialIconButton(
                    icon = navIcon,
                    onClick = onNavClick,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            title = {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    maxLines = 1,
                    style = Typography.titleLarge,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = onMenuClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            elevation = elevation
        )
    }

}