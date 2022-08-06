package com.linku.im.screen.chat.composable

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.Message
import com.linku.domain.entity.TextMessage
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.ui.components.TextImage
import com.linku.im.ui.theme.seed
import com.linku.im.vm
import org.ocpsoft.prettytime.PrettyTime
import java.util.*

private val HORIZONTAL_IN_PADDING = 12.dp
private val VERTICAL_IN_PADDING = 8.dp
private val HORIZONTAL_OUT_PADDING = 18.dp
private const val HORIZONTAL_OUT_PADDING_TIMES = 3
private val VERTICAL_OUT_PADDING = HORIZONTAL_OUT_PADDING / HORIZONTAL_OUT_PADDING_TIMES
private val BUBBLE_CORNER = 12.dp
private val BUBBLE_SPECIAL_CORNER = 0.dp

@Composable
fun ChatBubble(
    message: Message,
    config: BubbleConfig,
    modifier: Modifier = Modifier
) {
    val state by vm.state
    val isSystemInDarkMode = state.isDarkMode

    val isAnother = config.isAnother
    val backgroundColor: Color = if (isAnother)
        if (!isSystemInDarkMode) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.secondary
    val contentColor: Color = if (isAnother)
        if (!isSystemInDarkMode) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.onSecondary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        config.isShowTime.ifTrue {
            val locale = Locale.getDefault()
            val prettyTime = PrettyTime(locale)
            val display = prettyTime.format(Date(message.timestamp))
            Text(
                text = display ?: "",
                modifier = Modifier.padding(
                    horizontal = 4.dp,
                    vertical = 8.dp
                ),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier.padding(
                vertical = VERTICAL_OUT_PADDING
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
                horizontalArrangement = if (isAnother) Arrangement.Start else Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                when (config.sendState) {
                    Message.STATE_PENDING -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.Bottom)
                        )
                    }
                    Message.STATE_FAILED -> {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Failed",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Bottom)
                        )
                    }
                }
                if (config is BubbleConfig.Multi) {
                    if (config.avatarVisibility) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            TextImage(text = config.name, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    } else if (isAnother) {
                        Surface(modifier = Modifier.size(32.dp), color = Color.Transparent) {}
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }

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
                Card(
                    shape = shape,
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    )
                ) {
                    val customTextSelectionColors = TextSelectionColors(
                        handleColor = seed,
                        backgroundColor = ColorUtils.blendARGB(
                            backgroundColor.toArgb(),
                            Color.Black.toArgb(),
                            0.2f
                        ).let(::Color)
                    )
                    val nameVisibility = config is BubbleConfig.Multi && config.nameVisibility
                    if (nameVisibility) {
                        Text(
                            text = (config as BubbleConfig.Multi).name ?: "",
                            style = MaterialTheme.typography.titleSmall,
                            color = contentColor,
                            modifier = Modifier.padding(
                                start = HORIZONTAL_IN_PADDING,
                                end = HORIZONTAL_IN_PADDING,
                                top = VERTICAL_IN_PADDING,
                                bottom = 0.dp
                            )
                        )
                    }
                    when (message) {
                        is TextMessage -> {
                            CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                SelectionContainer {
                                    Text(
                                        text = message.text,
                                        color = contentColor,
                                        modifier = Modifier.padding(
                                            start = HORIZONTAL_IN_PADDING,
                                            end = HORIZONTAL_IN_PADDING,
                                            top = if (nameVisibility) VERTICAL_IN_PADDING / 2 else VERTICAL_IN_PADDING,
                                            bottom = VERTICAL_IN_PADDING,
                                        ),
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        is ImageMessage -> {
                            Surface(
                                shape = RoundedCornerShape(5),
                                modifier = Modifier.padding(4.dp),
                                color = backgroundColor,
                                contentColor = contentColor
                            ) {
                                val url =
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(message.url)
                                        .crossfade(true)
                                        .build()
                                SubcomposeAsyncImage(
                                    model = url,
                                    contentDescription = "Image Message",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(4 / 3f),
                                    contentScale = ContentScale.Crop,
                                    error = {
                                        val throwableMessage =
                                            stringResource(R.string.load_image_failed) +
                                                    '\n' + it.result.throwable.message
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = throwableMessage,
                                                style = MaterialTheme.typography.displaySmall,
                                                color = contentColor,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    },
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.load_image_loading),
                                                style = MaterialTheme.typography.displaySmall,
                                                color = contentColor,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        is GraphicsMessage -> {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(5),
                                    modifier = Modifier.padding(4.dp),
                                    color = backgroundColor,
                                    contentColor = contentColor
                                ) {
                                    message.content
                                    val url =
                                        ImageRequest.Builder(LocalContext.current).data(message.url)
                                            .crossfade(true).build()
                                    SubcomposeAsyncImage(
                                        model = url,
                                        contentDescription = "Graphics Message",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(4 / 3f),
                                        contentScale = ContentScale.Crop,
                                        error = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.load_image_failed),
                                                    style = MaterialTheme.typography.displaySmall,
                                                    color = contentColor,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        },
                                        loading = {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = stringResource(id = R.string.load_image_loading),
                                                    style = MaterialTheme.typography.displaySmall,
                                                    color = contentColor,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    )
                                }
                                CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
                                    Text(
                                        text = message.text,
                                        color = contentColor,
                                        modifier = Modifier.padding(
                                            start = HORIZONTAL_IN_PADDING,
                                            end = HORIZONTAL_IN_PADDING,
                                            top = VERTICAL_IN_PADDING,
                                            bottom = VERTICAL_IN_PADDING,
                                        ),
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = stringResource(id = R.string.unknown_message_type),
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(
                                    start = HORIZONTAL_IN_PADDING,
                                    end = HORIZONTAL_IN_PADDING,
                                    top = if (nameVisibility) VERTICAL_IN_PADDING / 2 else VERTICAL_IN_PADDING,
                                    bottom = VERTICAL_IN_PADDING,
                                ),
                                textAlign = TextAlign.Start,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }
            }
            // tail
        }

    }
}

sealed class BubbleConfig(
    open val isAnother: Boolean, open val isShowTime: Boolean, open val sendState: Int
) {
    data class Pair(
        override val sendState: Int = Message.STATE_SEND,
        private val another: Boolean = false,
        override val isShowTime: Boolean = false
    ) : BubbleConfig(another, isShowTime, sendState)

    data class Multi(
        override val sendState: Int = Message.STATE_SEND,
        private val other: Boolean = false,
        override val isShowTime: Boolean = false,
        val avatarVisibility: Boolean = false,
        val nameVisibility: Boolean = false,
        val name: String? = null,
    ) : BubbleConfig(other, isShowTime, sendState)
}
