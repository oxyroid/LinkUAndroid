package com.linku.im.screen.chat.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import com.linku.im.ui.theme.LocalTheme

@Composable
fun ChatBackground(
    modifier: Modifier = Modifier
) {
    val color = LocalTheme.current.chatBackground
    Box(
        modifier.drawWithCache {
            onDrawWithContent {
                drawRect(
                    color = color
                )
            }
        }
    )
}