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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.linku.im.NavViewModel
import com.linku.im.screen.chat.composable.ChatBubble
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    navViewModel: NavViewModel
) {
    val context = LocalContext.current
    with(navViewModel) {
        rememberedIcon.value = Icons.Default.ArrowBack
        rememberedTitle.value = viewModel.chatState.value.title
        rememberedOnNavClick.value = {
            navController.popBackStack()
        }
        rememberedActions.value = {
            IconButton(
                onClick = {

                }
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
            }
        }
    }
    LaunchedEffect(true) {
        viewModel.toastEvent.collectLatest {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.connectToChat()
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.disconnect()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val state by viewModel.chatState
    val text by viewModel.messageTextState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            reverseLayout = true
        ) {
            items(state.messages) {
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

            IconButton(
                onClick = {

                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Star,
                    contentDescription = "Emoji",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            OutlinedTextField(
                value = text,
                onValueChange = {
                    viewModel.onTextChange(it)
                },
                Modifier
                    .weight(1f),
                placeholder = {
                    Text(text = "Type here..", color = MaterialTheme.colorScheme.onBackground)
                },
                enabled = !state.isSending

            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = {
                    viewModel.onMessage()
                    viewModel.onTextChange("")
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