package com.linku.im.screen.chat

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.domain.Auth
import com.linku.im.Constants
import com.linku.im.R
import com.linku.im.global.LinkUEvent
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.vm

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
) {
    val context = LocalContext.current
    val state by viewModel.state
    val listState = rememberLazyListState()

    SideEffect {
        if (cid == -1) vm.onEvent(LinkUEvent.PopBackStack)
    }
    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initial(cid))
    }
    LaunchedEffect(state.event) {
        state.event.handle {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.padding(
            top = 8.dp,
            start = 8.dp,
            end = 8.dp,
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background
                            ),
                            end = Offset(0.0f, Float.POSITIVE_INFINITY)
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                reverseLayout = true,
                state = listState
            ) {
                val minDuration = Constants.CHAT_LABEL_MIN_DURATION
                itemsIndexed(state.messages, key = { _, item -> item.id }) { index, it ->
                    val next = if (index == state.messages.size - 1) null
                    else state.messages[index + 1]
                    val showTimeLabel =
                        next == null || it.timestamp - next.timestamp >= minDuration
                    ChatBubble(
                        message = it,
                        isAnother = it.uid != Auth.currentUID,
                        isShowTime = showTimeLabel
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                color = MaterialTheme.colorScheme.background,
                elevation = 8.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    OutlinedTextField(
                        value = state.text,
                        onValueChange = {
                            viewModel.onEvent(ChatEvent.TextChange(it))
                        },
                        Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.screen_chat_input),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    AnimatedVisibility(visible = state.text.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(ChatEvent.SendTextMessage)
                            },
                        ) {
                            val imageVector = Icons.Rounded.Send
                            Icon(
                                imageVector = imageVector,
                                contentDescription = "send",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

        }
    }
    LaunchedEffect(state.scrollToBottom) {
        state.scrollToBottom.handle { listState.animateScrollToItem(0) }
    }
}