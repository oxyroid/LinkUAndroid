package com.linku.im.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TextImage(
    text: String?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 36.sp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.background(color),
        contentAlignment = Alignment.Center
    ) {
        if (!text.isNullOrBlank()) {
            Text(
                text = text.first().toString(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = fontSize
                ),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center,
            )
        }
    }
}