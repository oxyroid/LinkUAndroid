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
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.global.GlobalViewModel

@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
    vm: GlobalViewModel = hiltViewModel(),
    cid: Int?
) {
    checkNotNull(cid)
    val context = LocalContext.current
    with(vm) {
        icon.value = Icons.Default.ArrowBack
        title.value = viewModel.state.value.title
        navClick.value = {
            navController.popBackStack()
        }
        actions.value = {
            IconButton(
                onClick = {

                }
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
            }
        }
    }

    val state by viewModel.state
    LaunchedEffect(state.event) {
        state.event.handle {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.onEvent(
                    ChatEvent.InitChat(
                        cid = cid,
                        source = vm.messages
                    )
                )
            } else if (event == Lifecycle.Event.ON_STOP) {

            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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