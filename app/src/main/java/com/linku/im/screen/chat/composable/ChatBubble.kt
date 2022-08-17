package com.linku.im.screen.chat.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import coil.ImageLoader
import coil.compose.*
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.Message
import com.linku.domain.entity.TextMessage
import com.linku.im.R
import com.linku.im.extension.intervalClickable
import com.linku.im.extension.times
import com.linku.im.ui.components.TextImage
import com.linku.im.ui.theme.LocalExpandColor
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.vm
import java.util.*

private val HORIZONTAL_IN_PADDING = 12.dp
private val VERTICAL_IN_PADDING = 8.dp
private val HORIZONTAL_OUT_PADDING = 18.dp
private const val HORIZONTAL_OUT_PADDING_TIMES = 3
private val BUBBLE_CORNER = 12.dp
private val BUBBLE_SPECIAL_CORNER = 0.dp

@Composable
fun ChatBubble(
    message: Message,
    config: BubbleConfig,
    modifier: Modifier = Modifier,
    onPreview: (String) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit
) {
    val state = vm.readable
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
    Row(
        modifier = modifier
            .padding(
                start = HORIZONTAL_OUT_PADDING.let {
                    if (!isAnother) it * HORIZONTAL_OUT_PADDING_TIMES
                    else it
                },
                end = HORIZONTAL_OUT_PADDING.let {
                    if (isAnother) it * HORIZONTAL_OUT_PADDING_TIMES
                    else it
                },
                top = 6.dp,
                bottom = if (config.isEndOfGroup) 6.dp else 0.dp
            )
            .fillMaxWidth(),
        horizontalArrangement = if (isAnother) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        when (config.sendState) {
            Message.STATE_PENDING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(LocalSpacing.current.medium)
                        .align(Alignment.Bottom)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Message.STATE_FAILED -> {
                Icon(
                    imageVector = Icons.Sharp.Warning,
                    contentDescription = "Failed",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(LocalSpacing.current.large)
                        .align(Alignment.Bottom)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        if (config is BubbleConfig.Multi) {
            if (config.avatarVisibility) {
                SubcomposeAsyncImage(
                    model = config.avatar,
                    contentDescription = "",
                    modifier = Modifier
                        .size(LocalSpacing.current.largest)
                        .clip(RoundedCornerShape(50))
                        .intervalClickable { onProfile(message.uid) },
                    error = {
                        TextImage(
                            text = config.name,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .size(LocalSpacing.current.largest)
                                .clip(RoundedCornerShape(50))
                        )
                    },
                    loading = {
                        TextImage(
                            text = config.name,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .size(LocalSpacing.current.largest)
                                .clip(RoundedCornerShape(50))
                        )
                    },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(LocalSpacing.current.medium))
            } else if (isAnother) {
                Box(
                    modifier = Modifier
                        .size(LocalSpacing.current.largest)
                        .background(Color.Transparent)
                )
                Spacer(modifier = Modifier.width(LocalSpacing.current.medium))
            }
        }

        val shape = if (isAnother) RoundedCornerShape(
            topEnd = BUBBLE_CORNER,
            bottomStart = if (config.isEndOfGroup) BUBBLE_SPECIAL_CORNER else BUBBLE_CORNER,
            bottomEnd = BUBBLE_CORNER,
            topStart = BUBBLE_CORNER
        ) else RoundedCornerShape(
            topEnd = BUBBLE_CORNER,
            bottomStart = BUBBLE_CORNER,
            bottomEnd = if (config.isEndOfGroup) BUBBLE_SPECIAL_CORNER else BUBBLE_CORNER,
            topStart = BUBBLE_CORNER
        )
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            )
        ) {
            val customTextSelectionColors = TextSelectionColors(
                handleColor = LocalExpandColor.current.seed,
                backgroundColor = ColorUtils.blendARGB(
                    backgroundColor.toArgb(),
                    Color.Black.toArgb(),
                    0.2f
                ).let(::Color)
            )
            val nameVisibility = config is BubbleConfig.Multi && config.nameVisibility
            if (nameVisibility) {
                Text(
                    text = (config as BubbleConfig.Multi).name,
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
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                val reply = config.reply
                reply?.let {
                    Row(
                        modifier = Modifier
                            .intervalClickable {
                                if (it.index != -1) onScroll(it.index)
                            }
                            .padding(
                                start = HORIZONTAL_IN_PADDING,
                                end = HORIZONTAL_IN_PADDING,
                                top = if (nameVisibility) VERTICAL_IN_PADDING / 2
                                else VERTICAL_IN_PADDING
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .height(LocalSpacing.current.large)
                                .width(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(LocalContentColor.current)
                        )
                        Text(
                            text = it.display,
                            color = LocalContentColor.current * 0.8f,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
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
                                        top = if (reply == null) VERTICAL_IN_PADDING else VERTICAL_IN_PADDING / 2,
                                        bottom = VERTICAL_IN_PADDING,
                                    ),
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    is ImageMessage -> {
                        Image(
                            backgroundColor = backgroundColor,
                            contentColor = contentColor,
                            message = message,
                            contentDescription = "Image Message",
                            onClick = { onPreview(it) },
                            isPending = config.sendState == Message.STATE_PENDING
                        )
                    }
                    is GraphicsMessage -> {
                        Column {
                            Image(
                                backgroundColor = backgroundColor,
                                contentColor = contentColor,
                                message = message,
                                contentDescription = "Graphics Message",
                                onClick = { onPreview(it) },
                                isPending = config.sendState == Message.STATE_PENDING
                            )
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
                            color = MaterialTheme.colorScheme.primary,
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Image(
    backgroundColor: Color,
    contentColor: Color,
    message: Message,
    isPending: Boolean = false,
    contentDescription: String? = null,
    onClick: (String) -> Unit
) {
    val realUrl = when (message) {
        is ImageMessage -> message.url
        is GraphicsMessage -> message.url
        else -> ""
    }
    Surface(
        shape = RoundedCornerShape(5),
        modifier = Modifier.padding(4.dp),
        color = backgroundColor,
        contentColor = contentColor,
        onClick = { onClick(realUrl) }
    ) {
        val model = ImageRequest.Builder(LocalContext.current)
            .data(realUrl)
            .crossfade(200)
            .build()
        val loader = ImageLoader.Builder(LocalContext.current)
            .components {
                add(ImageDecoderDecoder.Factory())
            }
            .build()
        val painter = rememberAsyncImagePainter(
            model = model,
            imageLoader = loader
        )
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
                .apply {
                    if (isPending) {
                        blur(8.dp)
                    }
                },
            contentScale = ContentScale.Crop,
        )
    }
}

sealed class BubbleConfig(
    open val isAnother: Boolean,
    open val isShowTime: Boolean,
    open val sendState: Int,
    open val isEndOfGroup: Boolean,
    open val reply: ReplyConfig?
) {
    data class PM(
        override val sendState: Int = Message.STATE_SEND,
        private val another: Boolean = false,
        override val isShowTime: Boolean = false,
        override val isEndOfGroup: Boolean = false,
        override val reply: ReplyConfig? = null
    ) : BubbleConfig(another, isShowTime, sendState, isEndOfGroup, reply)

    data class Multi(
        override val sendState: Int = Message.STATE_SEND,
        private val other: Boolean = false,
        override val isShowTime: Boolean = false,
        val avatarVisibility: Boolean = false,
        val nameVisibility: Boolean = false,
        val name: String = "",
        val avatar: String = "",
        override val isEndOfGroup: Boolean = false,
        override val reply: ReplyConfig? = null
    ) : BubbleConfig(other, isShowTime, sendState, isEndOfGroup, reply)
}

data class ReplyConfig(
    val targetMid: Int,
    val index: Int,
    val display: String
)