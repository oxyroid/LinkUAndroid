package com.linku.im.screen.chat.composable

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.linku.domain.bean.Emoji
import com.linku.im.R
import com.linku.im.extension.ifTrue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomBar(
    text: String,
    uri: Uri?,
    emojis: List<Emoji>,
    expended: Boolean,
    onSend: () -> Unit,
    onFile: () -> Unit,
    onText: (String) -> Unit,
    onEmoji: (String) -> Unit,
    onExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember(::FocusRequester)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(
                    horizontal = 8.dp,
                    vertical = 4.dp
                )
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = { onExpanded(!expended) }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEmotions,
                            contentDescription = "emoji",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    OutlinedTextField(
                        value = text,
                        onValueChange = { onText(it) },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .animateContentSize { _, _ -> },
                        placeholder = {
                            Text(
                                text = stringResource(
                                    if (uri == null) R.string.screen_chat_input
                                    else R.string.screen_chat_input_image
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions { onSend() },

                        )

                    IconButton(onClick = onFile) {
                        Icon(
                            imageVector = Icons.Rounded.AttachFile,
                            contentDescription = "emoji",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            FilledIconButton(
                onClick = {
                    if (text.isNotBlank() || uri != null) onSend()
                }
            ) {
                Icon(
                    imageVector = (text.isNotBlank() || uri != null)
                        .ifTrue { Icons.Rounded.Send }
                        ?: Icons.Rounded.KeyboardVoice,
                    contentDescription = "send"
                )
            }

        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyHorizontalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height((expended.ifTrue { 144.dp } ?: 24.dp) + 24.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp),
            rows = GridCells.Fixed(expended.ifTrue { 4 } ?: 1),
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
            .padding(12.dp)
            .width(24.dp)
            .aspectRatio(1f)
            .clickable { onClick() }
    )
}