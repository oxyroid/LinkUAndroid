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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.linku.domain.entity.Message

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
                start = 12.let {
                    if (!isAnother) it * 3
                    else it
                }.dp,
                end = 12.let {
                    if (isAnother) it * 3
                    else it
                }.dp,
                top = 4.dp,
                bottom = 4.dp
            )
            .fillMaxWidth(),
        horizontalArrangement = if (isAnother) Arrangement.Start else Arrangement.End
    ) {
        val shape = if (isAnother) RoundedCornerShape(
            topEndPercent = 25,
            bottomStartPercent = 0,
            bottomEndPercent = 25,
            topStartPercent = 25
        ) else RoundedCornerShape(
            topEndPercent = 25,
            bottomStartPercent = 25,
            bottomEndPercent = 0,
            topStartPercent = 25
        )
        Surface(
            shape = shape,
            color = color,
        ) {
            Text(
                text = message.content,
                color = contentColor,
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                style = TextStyle()
            )
        }
    }
}