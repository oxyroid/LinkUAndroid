package com.linku.im.screen.chat.composable

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.KeyboardVoice
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.linku.core.ktx.ifTrue
import com.linku.domain.bean.Emoji
import com.linku.im.R
import com.linku.im.ktx.ui.graphics.times
import com.linku.im.ktx.ui.intervalClickable
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTextField(
    text: TextFieldValue,
    uri: Uri?,
    onSend: () -> Unit,
    onFile: () -> Unit,
    onText: (TextFieldValue) -> Unit,
    onExpanded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalTheme.current
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    horizontal = spacing.small,
                    vertical = spacing.extraSmall
                )
        ) {
            Surface(
                color = theme.surface,
                contentColor = theme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = spacing.extraSmall)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides theme.onSurface.copy(alpha = .35f)
                    ) {
                        MaterialIconButton(
                            icon = Icons.Rounded.EmojiEmotions,
                            onClick = { onExpanded() }
                        )
                        OutlinedTextField(
                            value = text,
                            onValueChange = onText,
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(),
                            placeholder = {
                                Text(
                                    text = when (uri) {
                                        null -> stringResource(R.string.screen_chat_input)
                                        else -> stringResource(R.string.screen_chat_input_image)
                                    },
                                    color = theme.onSurface * 0.65f,
                                    maxLines = 1,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.graphicsLayer {
                                        this.translationY = 4f
                                    }
                                )
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = theme.primary,
                                textColor = theme.onSurface
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
                            icon = Icons.Rounded.AttachFile,
                            onClick = onFile,
                            contentDescription = "attach file"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(spacing.extraSmall))

            FilledIconButton(
                onClick = {
                    if (text.text.isNotBlank() || uri != null) onSend()
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = theme.primary,
                    contentColor = theme.onPrimary
                ),
                modifier = Modifier
                    .clip(IconButtonDefaults.filledShape)
            ) {
                Icon(
                    imageVector = (text.text.isNotBlank() || uri != null)
                        .ifTrue { Icons.Rounded.Send }
                        ?: Icons.Rounded.KeyboardVoice,
                    contentDescription = "send"
                )
            }

        }

        Spacer(modifier = Modifier.height(spacing.medium))
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.EmojiButton(
    emoji: Emoji,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    AsyncImage(
        model = emoji.bitmap,
        contentDescription = null,
        modifier = modifier
            .animateItemPlacement()
            .padding(vertical = spacing.extraSmall)
            .clip(RoundedCornerShape(spacing.medium))
            .intervalClickable { onClick() }
            .padding(spacing.small)
            .aspectRatio(1f)
    )
}
