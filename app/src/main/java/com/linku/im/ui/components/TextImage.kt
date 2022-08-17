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
import com.linku.im.ui.theme.SugarColors

@Composable
fun TextImage(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    color: Color = SugarColors.key(text)
) {
    Box(
        modifier = modifier.background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.first().toString(),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = fontSize
            ),
            color = Color.White,
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center,
        )
    }
}