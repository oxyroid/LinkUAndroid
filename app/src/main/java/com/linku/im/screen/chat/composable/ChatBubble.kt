package com.linku.im.screen.chat.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.linku.domain.entity.Message
import java.util.*

private const val HORIZONTAL_IN_PADDING = 12
private const val VERTICAL_IN_PADDING = 8
private const val HORIZONTAL_OUT_PADDING = 18
private const val HORIZONTAL_OUT_PADDING_TIMES = 3
private const val VERTICAL_OUT_PADDING = HORIZONTAL_OUT_PADDING / HORIZONTAL_OUT_PADDING_TIMES
private const val BUBBLE_CORNER = 12
private const val BUBBLE_SPECIAL_CORNER = 0
private const val FONT_SIZE = 14

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier,
    isAnother: Boolean = false,
    isShowTime: Boolean = false,
    onClick: () -> Unit = {}
) {
    val color: Color =
        if (isAnother) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
    val contentColor: Color =
        if (isAnother) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
    ) {
        if (isShowTime) {
            Card(
                backgroundColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                onClick = onClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                val calender = Calendar.getInstance()
                calender.time = Date(message.timestamp)
                val hour = calender.get(Calendar.HOUR_OF_DAY)
                val minute = calender.get(Calendar.MINUTE)
                val text = buildString {
                    if (hour < 10) append("0$hour") else append(hour.toString())
                    append(":")
                    if (minute < 10) append("0$minute") else append(minute.toString())
                }
                Text(
                    text = text,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
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
                topEnd = BUBBLE_CORNER.dp,
                bottomStart = BUBBLE_SPECIAL_CORNER.dp,
                bottomEnd = BUBBLE_CORNER.dp,
                topStart = BUBBLE_CORNER.dp
            ) else RoundedCornerShape(
                topEnd = BUBBLE_CORNER.dp,
                bottomStart = BUBBLE_CORNER.dp,
                bottomEnd = BUBBLE_SPECIAL_CORNER.dp,
                topStart = BUBBLE_CORNER.dp
            )
            Surface(
                shape = shape,
                color = color,
            ) {
                val customTextSelectionColors = TextSelectionColors(
                    handleColor = color,
                    backgroundColor = ColorUtils.blendARGB(
                        color.toArgb(),
                        contentColor.toArgb(),
                        0.6f
                    ).let(::Color)
                )
                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                    SelectionContainer {
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
        }
    }
}