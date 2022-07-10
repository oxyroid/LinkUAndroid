package com.linku.im.screen.chat.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.domain.entity.Message

private const val HORIZONTAL_IN_PADDING = 12
private const val VERTICAL_IN_PADDING = 8
private const val HORIZONTAL_OUT_PADDING = 18
private const val HORIZONTAL_OUT_PADDING_TIMES = 3
private const val VERTICAL_OUT_PADDING = HORIZONTAL_OUT_PADDING / HORIZONTAL_OUT_PADDING_TIMES
private const val BUBBLE_CORNER_PERCENT = 25
private const val BUBBLE_SPECIAL_CORNER_PERCENT = 0
private const val FONT_SIZE = 14

@Composable
fun ChatBubble(
    message: Message,
    isAnother: Boolean = false
) {
    val color: Color =
        if (isAnother) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val contentColor: Color =
        if (isAnother) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = Modifier
            .padding(
                start = HORIZONTAL_OUT_PADDING.let {
                    if (!isAnother) it * HORIZONTAL_OUT_PADDING_TIMES
                    else it
                }.dp,
                end = HORIZONTAL_OUT_PADDING.let {
                    if (isAnother) it * HORIZONTAL_OUT_PADDING_TIMES
                    else it
                }.dp,
                top = VERTICAL_OUT_PADDING.dp,
                bottom = VERTICAL_OUT_PADDING.dp
            )
            .fillMaxWidth(),
        horizontalArrangement = if (isAnother) Arrangement.Start else Arrangement.End
    ) {
        val shape = if (isAnother) RoundedCornerShape(
            topEndPercent = BUBBLE_CORNER_PERCENT,
            bottomStartPercent = BUBBLE_SPECIAL_CORNER_PERCENT,
            bottomEndPercent = BUBBLE_CORNER_PERCENT,
            topStartPercent = BUBBLE_CORNER_PERCENT
        ) else RoundedCornerShape(
            topEndPercent = BUBBLE_CORNER_PERCENT,
            bottomStartPercent = BUBBLE_CORNER_PERCENT,
            bottomEndPercent = BUBBLE_SPECIAL_CORNER_PERCENT,
            topStartPercent = BUBBLE_CORNER_PERCENT
        )
        Surface(
            shape = shape,
            color = color,
        ) {
            Text(
                text = message.content,
                color = contentColor,
                modifier = Modifier
                    .padding(
                        vertical = VERTICAL_IN_PADDING.dp,
                        horizontal = HORIZONTAL_IN_PADDING.dp
                    ),
                textAlign = TextAlign.Start,
                fontSize = FONT_SIZE.sp
            )
        }
    }
}