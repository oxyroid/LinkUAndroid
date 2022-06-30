package com.linku.im.ui

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MaterialIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = icon.name
) {
    IconButton(onClick = onClick, modifier) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}