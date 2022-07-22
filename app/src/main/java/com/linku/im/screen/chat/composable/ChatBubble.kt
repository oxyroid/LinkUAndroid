package com.linku.im.screen.chat.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.linku.domain.entity.Message
import com.linku.im.extension.ifTrue
import com.linku.im.ui.theme.Fonts
import com.linku.im.ui.theme.seed
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
        if (isAnother)
            if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.onBackground
        else MaterialTheme.colorScheme.secondary
    val contentColor: Color =
        if (isAnother)
            if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.background
        else MaterialTheme.colorScheme.onSecondary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        isShowTime.ifTrue {
            Timestamp(timestamp = message.timestamp)
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
                BubbleText(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BubbleText(
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
    ElevatedCard(
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = color
        ),
    ) {
        val customTextSelectionColors = TextSelectionColors(
            handleColor = seed,
            backgroundColor = ColorUtils.blendARGB(
                color.toArgb(),
                Color.Black.toArgb(),
                0.2f
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
private fun Timestamp(timestamp: Long) {
    Row(
        modifier = Modifier
            .padding(top = VERTICAL_IN_PADDING)
            .fillMaxWidth()
            .padding(vertical = VERTICAL_IN_PADDING),
        horizontalArrangement = Arrangement.Center
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
                horizontal = 4.dp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = Fonts.Medium.toFontFamily()
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}