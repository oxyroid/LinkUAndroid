package com.linku.im.screen.setting.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.linku.im.ktx.ui.graphics.animated
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeAddSelection(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalTheme.current
    Box(
        contentAlignment = Alignment.Center
    ) {
        OutlinedCard(
            colors = CardDefaults.outlinedCardColors(
                containerColor = theme.surface.animated(),
                contentColor = theme.onSurface.animated()
            ),
            elevation = CardDefaults.outlinedCardElevation(
                defaultElevation = LocalSpacing.current.none
            ),
            modifier = modifier
                .graphicsLayer {
                    scaleX = 0.8f
                    scaleY = 0.8f
                }
                .aspectRatio(1f)
                .padding(LocalSpacing.current.extraSmall),
            onClick = onClick,
            content = {}
        )

        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "",
            tint = theme.onSurface.animated()
        )
    }
}