package com.linku.im.screen.chat.composable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.linku.domain.entity.Message
import com.linku.im.extension.ifTrue
import java.util.*

private val HORIZONTAL_IN_PADDING = 12.dp
private val VERTICAL_IN_PADDING = 8.dp
private val HORIZONTAL_OUT_PADDING = 18.dp
private const val HORIZONTAL_OUT_PADDING_TIMES = 3
private val VERTICAL_OUT_PADDING = HORIZONTAL_OUT_PADDING / HORIZONTAL_OUT_PADDING_TIMES
private val BUBBLE_CORNER = 12.dp
private val BUBBLE_SPECIAL_CORNER = 0.dp
private val FONT_SIZE = 14.sp

@Composable
fun ChatBubble(
    message: Message,
    modifier: Modifier = Modifier,
    isAnother: Boolean = false,
    isShowTime: Boolean = false
) {
    val color: Color =
        if (isAnother) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val contentColor: Color =
        if (isAnother) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        isShowTime.ifTrue {
            TimestampCard(timestamp = message.timestamp)
        }
        Box(
            modifier = Modifier.padding(
                top = VERTICAL_OUT_PADDING,
                bottom = VERTICAL_OUT_PADDING
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        start = HORIZONTAL_OUT_PADDING.let {
                            if (!isAnother) it * HORIZONTAL_OUT_PADDING_TIMES
                            else it
                        },
                        end = HORIZONTAL_OUT_PADDING.let {
                            if (isAnother) it * HORIZONTAL_OUT_PADDING_TIMES
                            else it
                        },
                    )
                    .fillMaxWidth(),
                horizontalArrangement = if (isAnother) Arrangement.Start else Arrangement.End
            ) {
                when (message.sendState) {
                    Message.STATE_PENDING -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Bottom)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Message.STATE_FAILED -> {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Bottom)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
                BubbleTextField(
                    text = message.content,
                    isAnother = isAnother,
                    color = color,
                    contentColor = contentColor
                )
            }
            // tail
        }

    }
}

@Composable
private fun BubbleTextField(
    text: String,
    isAnother: Boolean,
    color: Color,
    contentColor: Color
) {
    val shape = if (isAnother) RoundedCornerShape(
        topEnd = BUBBLE_CORNER,
        bottomStart = BUBBLE_SPECIAL_CORNER,
        bottomEnd = BUBBLE_CORNER,
        topStart = BUBBLE_CORNER
    ) else RoundedCornerShape(
        topEnd = BUBBLE_CORNER,
        bottomStart = BUBBLE_CORNER,
        bottomEnd = BUBBLE_SPECIAL_CORNER,
        topStart = BUBBLE_CORNER
    )
    Surface(
        shape = shape,
        color = color,
        elevation = 1.dp
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
                    text = text,
                    color = contentColor,
                    modifier = Modifier
                        .padding(
                            vertical = VERTICAL_IN_PADDING,
                            horizontal = HORIZONTAL_IN_PADDING
                        ),
                    textAlign = TextAlign.Start,
                    fontSize = FONT_SIZE
                )
            }
        }
    }
}

@Composable
private fun TimestampCard(timestamp: Long) {
    Card(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(BUBBLE_CORNER),
        modifier = Modifier.padding(top = VERTICAL_IN_PADDING)
    ) {
        val calender = Calendar.getInstance()
        calender.time = Date(timestamp)
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
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}