package com.linku.im.screen.chat.composable

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.AttachFile
import androidx.compose.material.icons.sharp.EmojiEmotions
import androidx.compose.material.icons.sharp.KeyboardVoice
import androidx.compose.material.icons.sharp.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.linku.domain.bean.Emoji
import com.linku.im.R
import com.linku.im.ktx.ifTrue
import com.linku.im.ktx.compose.ui.intervalClickable
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTextField(
    text: () -> TextFieldValue,
    uri: () -> Uri?,
    emojis: List<Emoji>,
    emojiSpanExpanded:() -> Boolean,
    onSend: () -> Unit,
    onFile: () -> Unit,
    onText: (TextFieldValue) -> Unit,
    onEmoji: (String) -> Unit,
    onExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    horizontal = LocalSpacing.current.small,
                    vertical = LocalSpacing.current.extraSmall
                )
        ) {
            // Real Text Field
            Surface(
                color = LocalTheme.current.surface,
                contentColor = LocalTheme.current.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = LocalSpacing.current.extraSmall)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides LocalTheme.current.onSurface.copy(alpha = .35f)
                    ) {
                        MaterialIconButton(
                            icon = Icons.Sharp.EmojiEmotions,
                            onClick = { onExpanded(!emojiSpanExpanded()) }
                        )
                        OutlinedTextField(
                            value = text(),
                            onValueChange = onText,
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(),
                            placeholder = {
                                Text(
                                    text = when (uri()) {
                                        null -> stringResource(R.string.screen_chat_input)
                                        else -> stringResource(R.string.screen_chat_input_image)
                                    },
                                    color = LocalTheme.current.onSurface,
                                    maxLines = 1,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = LocalTheme.current.primary,
                                textColor = LocalTheme.current.onSurface
                            ),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { onSend() }
                            )
                        )

                        MaterialIconButton(
                            icon = Icons.Sharp.AttachFile,
                            onClick = onFile,
                            contentDescription = "attach file"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(LocalSpacing.current.extraSmall))
            FilledIconButton(
                onClick = {
                    if (text().text.isNotBlank() || uri() != null) onSend()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = LocalTheme.current.primary,
                    contentColor = LocalTheme.current.onPrimary
                ),
                modifier = Modifier
                    .clip(IconButtonDefaults.filledShape)
            ) {
                Icon(
                    imageVector = (text().text.isNotBlank() || uri() != null)
                        .ifTrue { Icons.Sharp.Send }
                        ?: Icons.Sharp.KeyboardVoice,
                    contentDescription = "send"
                )
            }

        }

        Spacer(modifier = Modifier.height(LocalSpacing.current.medium))

        LazyHorizontalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height((emojiSpanExpanded().ifTrue { 144.dp }
                    ?: LocalSpacing.current.large) + LocalSpacing.current.large)
                .background(LocalTheme.current.surface)
                .padding(vertical = LocalSpacing.current.small),
            rows = GridCells.Fixed(emojiSpanExpanded().ifTrue { 4 } ?: 1),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Start
        ) {
            items(items = emojis) {
                EmojiButton(it) { onEmoji(it.emoji) }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyGridItemScope.EmojiButton(
    emoji: Emoji,
    onClick: () -> Unit
) {
    AsyncImage(
        model = emoji.bitmap,
        contentDescription = "",
        modifier = Modifier
            .animateItemPlacement()
            .padding(LocalSpacing.current.medium)
            .width(LocalSpacing.current.large)
            .aspectRatio(1f)
            .intervalClickable { onClick() }
    )
}