package com.linku.im.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@Composable
fun Tip(
    text: String,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(
        LocalContentAlpha provides ContentAlpha.disabled
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = LocalTheme.current.onSurface * 0.6f,
            modifier = modifier
                .fillMaxWidth()
                .background(LocalTheme.current.surface)
                .padding(
                    horizontal = LocalSpacing.current.medium,
                    vertical = LocalSpacing.current.extraSmall
                )
        )
    }
}