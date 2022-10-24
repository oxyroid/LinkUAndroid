package com.linku.im.ui.components.button

import androidx.compose.animation.Crossfade
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MaterialIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = remember(icon) { icon.name }
) {
    IconButton(onClick, modifier) {
        Crossfade(targetState = icon) { icon ->
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint
            )
        }
    }
}
