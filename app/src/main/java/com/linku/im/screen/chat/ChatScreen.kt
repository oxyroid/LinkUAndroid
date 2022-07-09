package com.linku.im.screen.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.im.overall
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.overall.OverallEvent

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
) {
    if (cid == -1) overall.onEvent(OverallEvent.PopBackStack)
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.onEvent(ChatEvent.InitChat(cid, overall.messages))
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
        val messages by viewModel.messages.collectAsState(initial = null)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            reverseLayout = true
        ) {
            items(messages ?: emptyList()) {
                ChatBubble(message = it, isAnother = it.uid != 1)
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
                enabled = !state.sending
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    viewModel.onEvent(ChatEvent.SendTextMessage)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "send",
                    tint = MaterialTheme.colorScheme.surfaceTint
                )
            }
        }

    }
}