package com.linku.im.screen.chat.composable

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.linku.im.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ChatBottomBar(
    text: String,
    image: Bitmap?,
    onSend: () -> Unit,
    onFile: () -> Unit,
    onText: (String) -> Unit,
    clearUri: () -> Unit
) {
    var lastImage by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .navigationBarsPadding()
            .imePadding()
    ) {
        AnimatedVisibility(
            visible = image != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring()
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring()
            )
        ) {
            Surface(
                shape = RoundedCornerShape(5),
                modifier = Modifier.padding(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box {
                    val imageBitmap = image?.asImageBitmap()
                    if (imageBitmap != null) lastImage = imageBitmap
                    Image(
                        bitmap = lastImage!!,
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4 / 3f),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = clearUri,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_close_24),
                            contentDescription = "",
                            tint = LocalContentColor.current
                        )
                    }
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = {
                    onText(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize { _, _ -> },
                placeholder = {
                    Text(
                        text = stringResource(
                            if (image == null) R.string.screen_chat_input
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
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSend()
                    }
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            AnimatedContent(
                targetState = text.isNotBlank() || image == null,
                transitionSpec = { scaleIn() with scaleOut() }
            ) {
                IconButton(
                    onClick = {
                        if (text.isNotBlank() || image != null) onSend()
                        else onFile()
                    },
                ) {
                    if (text.isNotBlank() || image != null) {
                        val imageVector = Icons.Rounded.Send
                        Icon(
                            imageVector = imageVector,
                            contentDescription = "send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sharp_attach_file_24),
                            contentDescription = "file",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}