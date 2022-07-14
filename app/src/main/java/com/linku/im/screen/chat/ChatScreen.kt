package com.linku.im.screen.chat

import android.widget.Toast
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.domain.Auth
import com.linku.im.overall
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.overall.OverallEvent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
) {
    if (cid == -1) overall.onEvent(OverallEvent.PopBackStack)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.InitChat(cid))
    }
    val state by viewModel.state
    LaunchedEffect(state.event) {
        state.event.handle {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true,
        ) {
            itemsIndexed(state.messages, key = { _, item -> item.id }) { index, it ->
                val next = if (index == state.messages.size - 1) null else state.messages[index + 1]
                val showTimeLabel =
                    next == null || it.timestamp - next.timestamp >= 1000 * 60 * 5
                ChatBubble(
                    message = it,
                    isAnother = it.uid != Auth.currentUID,
                    isShowTime = showTimeLabel,
                    modifier = Modifier.animateItemPlacement(
                        animationSpec = spring()
                    )
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .imePadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = state.text,
                onValueChange = {
                    viewModel.onEvent(ChatEvent.TextChange(it))
                },
                Modifier
                    .weight(1f),
                placeholder = {
                    Text(text = "Type here..", color = MaterialTheme.colorScheme.onBackground)
                },
                enabled = !state.sending,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    viewModel.onEvent(ChatEvent.SendTextMessage)
                },
                enabled = !state.sending
            ) {
                val imageVector = if (state.sending) Icons.Rounded.Refresh else Icons.Rounded.Send
                Icon(
                    imageVector = imageVector,
                    contentDescription = "send",
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }

    }
}