package com.linku.im.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.ui.theme.SugarColors

@Composable
fun TextImage(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    color: Color = SugarColors.key(text)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawRect(color)
                drawContent()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.firstOrNull()?.toString() ?: "",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = fontSize,
                baselineShift = BaselineShift.None,
                fontWeight = FontWeight(1000)
            ),
            color = LocalTheme.current.background,
            textAlign = TextAlign.Center
        )
    }
}