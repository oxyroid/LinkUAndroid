package com.linku.im.screen.chat

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.domain.entity.Conversation
import com.linku.im.Constants
import com.linku.im.extension.ifTrue
import com.linku.im.linku.LinkUEvent
import com.linku.im.screen.chat.composable.ChatBottomBar
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.vm

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(), cid: Int
) {
    val context = LocalContext.current
    val state by viewModel.state
    val listState = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(ChatEvent.OnFileUriChange(uri))
    }
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            it.ifTrue { launcher.launch("image/*") }
        }

    vm.onActions {}

    vm.onTitle {
        Text(
            text = state.title,
            style = MaterialTheme.typography.titleMedium
        )
    }
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
    val firstVisibleItemIndex by derivedStateOf {
        listState.firstVisibleItemIndex
    }
    LaunchedEffect(firstVisibleItemIndex) {
        viewModel.onEvent(ChatEvent.FirstVisibleIndex(listState.firstVisibleItemIndex))
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true,
            state = listState
        ) {
            val minDuration = Constants.CHAT_LABEL_MIN_DURATION
            itemsIndexed(state.messages) { index, it ->
                val next = if (index == state.messages.size - 1) null
                else state.messages[index + 1]
                val showTimeLabel = next == null || it.timestamp - next.timestamp >= minDuration
                ChatBubble(
                    content = it.content,
                    isAnother = it.isAnother,
                    isShowTime = showTimeLabel,
                    sendState = it.sendState,
                    timestamp = it.timestamp,
                    avatar = it.avatar,
                    name = it.name,
                    isShowName = it.isShowName,
                    isMultiGroup = state.type == Conversation.TYPE_GROUP
                )
            }
        }
        ChatBottomBar(text = state.text,
            image = state.image,
            onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
            onText = { viewModel.onEvent(ChatEvent.TextChange(it)) },
            onFile = { permissionState.launchPermissionRequest() },
            clearUri = { viewModel.onEvent(ChatEvent.OnFileUriChange(null)) }
        )
    }

    LaunchedEffect(state.scrollToBottom) {
        state.scrollToBottom.handle { listState.animateScrollToItem(0) }
    }
}