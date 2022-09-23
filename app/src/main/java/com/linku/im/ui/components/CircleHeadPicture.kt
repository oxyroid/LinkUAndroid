package com.linku.im.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import coil.compose.SubcomposeAsyncImage
import com.linku.im.ui.theme.LocalSpacing

@Composable
fun CircleHeadPicture(
    model: Any?,
    modifier: Modifier = Modifier,
    placeholder: @Composable () -> Unit = {}
) {
    SubcomposeAsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(
                width = LocalSpacing.current.extraSmall,
                shape = CircleShape,
                color = Color.Unspecified
            ),
        error = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                placeholder()
            }
        },
        loading = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                placeholder()
            }
        }
    )

}
