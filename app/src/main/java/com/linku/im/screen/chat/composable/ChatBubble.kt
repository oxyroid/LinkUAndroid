package com.linku.im.screen.chat.composable

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Cancel
import androidx.compose.material.icons.sharp.ContentCopy
import androidx.compose.material.icons.sharp.Refresh
import androidx.compose.material.icons.sharp.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.constraintlayout.compose.*
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.linku.domain.bean.Bubble
import com.linku.domain.bean.ComposeTheme
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.Message
import com.linku.domain.entity.TextMessage
import com.linku.im.R
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ktx.compose.ui.intervalClickable
import com.linku.im.ktx.ifTrue
import com.linku.im.ktx.rememberedRun
import com.linku.im.ui.components.item.TextImage
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

private val HORIZONTAL_IN_PADDING = 12.dp
private val VERTICAL_IN_PADDING = 8.dp
private val HORIZONTAL_OUT_PADDING = 18.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: Message,
    configProvider: () -> Bubble,
    focusIdProvider: () -> Int?,
    modifier: Modifier = Modifier,
    onImagePreview: (String, Rect) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit,
    onReply: (Int) -> Unit,
    onResend: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onFocus: (Int) -> Unit,
    onDismissFocus: () -> Unit,
    theme: ComposeTheme = LocalTheme.current
) {
    val hasFocus = rememberedRun(focusIdProvider) { invoke() == message.id }
    val config = configProvider()
    val isAnother = config.isAnother
    val isEndOfGroup = config.isEndOfGroup
    val backgroundColor: Color = remember(theme, isAnother) {
        if (isAnother) theme.bubbleStart
        else {
            if (config.sendState == Message.STATE_FAILED) theme.error
            else theme.bubbleEnd
        }
    }
    val contentColor: Color = remember(theme, isAnother) {
        if (isAnother) theme.onBubbleStart
        else {
            if (config.sendState == Message.STATE_FAILED) theme.onError
            else theme.onBubbleEnd
        }
    }
    val lowerHorizontalOutPadding = remember { HORIZONTAL_OUT_PADDING }
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = lowerHorizontalOutPadding)
    ) {
        val (_, image, card) = createRefs()

        val hapticFeedback = LocalHapticFeedback.current

        // HeadPic
        HeadPicView(
            reference = image,
            message = message,
            config = config,
            onProfile = onProfile
        )

        // Others
        Row(
            modifier = Modifier
                .constrainAs(card) {
                    start.linkTo(
                        if (isAnother) image.end else parent.start,
                        if (isAnother) lowerHorizontalOutPadding else lowerHorizontalOutPadding
                    )
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            horizontalArrangement = if (isAnother) Arrangement.Start else Arrangement.End
        ) {
            var boundaries: Rect by remember { mutableStateOf(Rect.Zero) }
            var aspectRatio: Float by remember { mutableStateOf(4 / 3f) }
            ElevatedCard(
                shape = RoundedCornerShape(
                    bottomStart = if (isAnother && isEndOfGroup) 0.dp else 12.dp,
                    bottomEnd = if (!isAnother && isEndOfGroup) 0.dp else 12.dp,
                    topEnd = 12.dp,
                    topStart = 12.dp
                ),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = backgroundColor,
                    contentColor = contentColor
                )
            ) {
                ConstraintLayout(
                    modifier = Modifier
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = LocalIndication.current,
                            onClick = { },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onFocus(message.id)
                            }
                        )
                ) {
                    val nameVisibility = config is Bubble.Group && config.nameVisibility
                    val (label, reply, content) = createRefs()
                    Text(
                        text = if (config is Bubble.Group) config.name else "",
                        style = MaterialTheme.typography.titleSmall,
                        color = contentColor,
                        modifier = Modifier
                            .constrainAs(label) {
                                visibility =
                                    nameVisibility.ifTrue { Visibility.Visible }
                                        ?: Visibility.Gone
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                                centerHorizontallyTo(parent, 0f)
                                top.linkTo(parent.top, VERTICAL_IN_PADDING)
                                width = Dimension.wrapContent
                            }
                            .padding(horizontal = HORIZONTAL_IN_PADDING)

                    )

                    val replyRemembered = config.reply
                    Row(
                        modifier = Modifier
                            .constrainAs(reply) {
                                visibility = (replyRemembered != null)
                                    .ifTrue { Visibility.Visible } ?: Visibility.Gone
                                start.linkTo(label.start)
                                top.linkTo(label.bottom)
                            }
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = LocalIndication.current,
                                onClick = {
                                    if (replyRemembered != null && replyRemembered.index != -1) onScroll(
                                        replyRemembered.index
                                    )
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onFocus(message.id)
                                }
                            )
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
                                .clip(CircleShape)
                                .background(LocalContentColor.current)
                        )
                        Text(
                            text = replyRemembered?.display ?: "",
                            color = LocalContentColor.current * 0.8f,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Box(
                        modifier = Modifier.constrainAs(content) {
                            top.linkTo(reply.bottom)
                            start.linkTo(reply.start)
                            bottom.linkTo(parent.bottom)
                        }
                    ) {
                        when (message) {
                            is TextMessage -> {
                                Text(
                                    text = message.text,
                                    color = contentColor,
                                    modifier = Modifier
                                        .padding(
                                            start = HORIZONTAL_IN_PADDING,
                                            end = HORIZONTAL_IN_PADDING,
                                            top = if (replyRemembered == null) VERTICAL_IN_PADDING else VERTICAL_IN_PADDING / 2,
                                            bottom = VERTICAL_IN_PADDING,
                                        ),
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            is ImageMessage -> {
                                ThumbView(
                                    backgroundColor = backgroundColor,
                                    contentColor = contentColor,
                                    message = message,
                                    contentDescription = "Image Message",
                                    isPending = rememberedRun(config.sendState) {
                                        this == Message.STATE_PENDING
                                    },
                                    modifier = Modifier
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = LocalIndication.current,
                                            onClick = {
                                                onImagePreview(message.url, boundaries)
                                            },
                                            onLongClick = {
                                                hapticFeedback.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                                onFocus(message.id)
                                            }
                                        ),
                                    onGloballyPositioned = { _boundaries, _aspectRatio ->
                                        boundaries = _boundaries
                                        aspectRatio = _aspectRatio
                                    }
                                )
                            }

                            is GraphicsMessage -> {
                                Column {
                                    ThumbView(
                                        backgroundColor = backgroundColor,
                                        contentColor = contentColor,
                                        message = message,
                                        contentDescription = "Graphics Message",
                                        isPending = config.sendState == Message.STATE_PENDING,
                                        modifier = Modifier
                                            .combinedClickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = LocalIndication.current,
                                                onClick = {
                                                    onImagePreview(message.url, boundaries)
                                                },
                                                onLongClick = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.LongPress
                                                    )
                                                    onFocus(message.id)
                                                }
                                            ),
                                        onGloballyPositioned = { _boundaries, _aspectRatio ->
                                            boundaries = _boundaries
                                            aspectRatio = _aspectRatio
                                        }
                                    )
                                    Text(
                                        text = message.text,
                                        color = contentColor,
                                        modifier = Modifier
                                            .padding(
                                                start = HORIZONTAL_IN_PADDING,
                                                end = HORIZONTAL_IN_PADDING,
                                                top = VERTICAL_IN_PADDING,
                                                bottom = VERTICAL_IN_PADDING,
                                            ),
                                        textAlign = TextAlign.Start,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            else -> {
                                Text(
                                    text = stringResource(id = R.string.unknown_message_type),
                                    color = contentColor,
                                    modifier = Modifier.padding(
                                        start = HORIZONTAL_IN_PADDING,
                                        end = HORIZONTAL_IN_PADDING,
                                        top = if (nameVisibility) VERTICAL_IN_PADDING / 2 else VERTICAL_IN_PADDING,
                                        bottom = VERTICAL_IN_PADDING,
                                    ),
                                    textAlign = TextAlign.Start,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                DropdownMenu(
                    expanded = hasFocus,
                    onDismissRequest = {
                        onDismissFocus()
                    },
                    offset = DpOffset(
                        x = LocalSpacing.current.extraSmall,
                        y = LocalSpacing.current.extraSmall
                    ),
                    modifier = Modifier.drawWithContent {
                        drawRect(theme.surface)
                        drawContent()
                    }
                ) {
                    val clipboardManager = LocalClipboardManager.current
                    when (message) {
                        is TextMessage -> {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dropdown_copy)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Sharp.ContentCopy,
                                        contentDescription = null
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = theme.onSurface,
                                    leadingIconColor = theme.onSurface,
                                    trailingIconColor = theme.onSurface
                                ),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    onDismissFocus()
                                }
                            )
                        }

                        is GraphicsMessage -> {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dropdown_copy)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Sharp.ContentCopy,
                                        contentDescription = null
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = theme.onSurface,
                                    leadingIconColor = theme.onSurface,
                                    trailingIconColor = theme.onSurface
                                ),
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(message.text))
                                    onDismissFocus()
                                }
                            )
                        }

                        else -> {}
                    }

                    when (message.sendState) {
                        Message.STATE_SEND, Message.STATE_ANOTHER -> {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.dropdown_reply)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Sharp.Reply,
                                        contentDescription = null
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = theme.onSurface,
                                    leadingIconColor = theme.onSurface,
                                    trailingIconColor = theme.onSurface
                                ),
                                onClick = {
                                    onDismissFocus()
                                    onReply(message.id)
                                }
                            )
                        }

                        Message.STATE_PENDING -> {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.dropdown_cancel)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Sharp.Cancel,
                                        contentDescription = null
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = theme.onSurface,
                                    leadingIconColor = theme.onSurface,
                                    trailingIconColor = theme.onSurface
                                ),
                                onClick = {
                                    onDismissFocus()
                                    onCancel(message.id)
                                }
                            )
                        }

                        Message.STATE_FAILED -> {
                            DropdownMenuItem(
                                text = { Text(stringResource(id = R.string.dropdown_retry)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Sharp.Refresh,
                                        contentDescription = null
                                    )
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = theme.onSurface,
                                    leadingIconColor = theme.onSurface,
                                    trailingIconColor = theme.onSurface
                                ),
                                onClick = {
                                    onDismissFocus()
                                    onResend(message.id)
                                }
                            )
                        }
                    }

                }
            }
        }

    }
}

@Composable
private fun ConstraintLayoutScope.HeadPicView(
    reference: ConstrainedLayoutReference,
    message: Message,
    config: Bubble,
    onProfile: (Int) -> Unit,
) {
    Box(
        modifier = Modifier.constrainAs(reference) {
            start.linkTo(parent.start)
            centerHorizontallyTo(parent, 0f)
            bottom.linkTo(parent.bottom)
        },
        contentAlignment = Alignment.Center
    ) {
        when (config) {
            is Bubble.Group -> {
                val isAnother = config.isAnother
                if (config.avatarVisibility) {
                    SubcomposeAsyncImage(
                        model = config.avatar,
                        contentDescription = "",
                        modifier = Modifier
                            .size(LocalSpacing.current.largest)
                            .clip(CircleShape)
                            .intervalClickable { onProfile(message.uid) },
                        error = {
                            TextImage(
                                text = config.name,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .size(LocalSpacing.current.largest)
                                    .clip(CircleShape)
                            )
                        },
                        loading = {
                            TextImage(
                                text = config.name,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .size(LocalSpacing.current.largest)
                                    .clip(CircleShape)
                            )
                        },
                        contentScale = ContentScale.Crop
                    )
                } else if (isAnother) {
                    Box(
                        modifier = Modifier
                            .size(LocalSpacing.current.largest)
                            .background(Color.Transparent)
                    )
                }
            }

            is Bubble.PM -> {}
        }
    }
}


@Composable
private fun ThumbView(
    backgroundColor: Color,
    contentColor: Color,
    message: Message,
    modifier: Modifier = Modifier,
    isPending: Boolean = false,
    contentDescription: String? = null,
    onGloballyPositioned: (Rect, Float) -> Unit
) {
    val realUrl = rememberedRun(message) {
        when (this) {
            is ImageMessage -> url
            is GraphicsMessage -> url
            else -> ""
        }
    }
    val withRatio: Boolean = rememberedRun(message) {
        when (this) {
            is ImageMessage -> {
                val width = width.toFloat()
                val height = height.toFloat()
                width > 0 && height > 0f
            }

            is GraphicsMessage -> {
                val width = width.toFloat()
                val height = height.toFloat()
                width > 0 && height > 0f
            }

            else -> false
        }
    }
    val aspectRatio = rememberedRun(message) {
        val defaultRatio = 4 / 3f
        if (withRatio) {
            when (this) {
                is ImageMessage -> width.toFloat() / height.toFloat()
                is GraphicsMessage -> width.toFloat() / height.toFloat()
                else -> defaultRatio
            }
        } else defaultRatio
    }
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp

    val (w, h) = rememberedRun(aspectRatio) {
        when {
            this >= 1f -> 0.75 * screenWidthDp to 0.75 * screenWidthDp / this
            else -> 0.45 * screenWidthDp to 0.45 * screenWidthDp / this
        }
    }
    Surface(
        shape = RoundedCornerShape(5),
        modifier = modifier
            .padding(LocalSpacing.current.extraSmall)
            .size(w, h),
        color = backgroundColor,
        contentColor = contentColor
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
        val blurEffect = remember { BlurEffect(16f, 16f) }
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = Modifier
                .graphicsLayer { if (isPending) renderEffect = blurEffect }
                .onGloballyPositioned {
                    onGloballyPositioned(it.boundsInWindow(), aspectRatio)
                },
            contentScale = if (withRatio) ContentScale.Fit else ContentScale.Crop
        )
    }
}
