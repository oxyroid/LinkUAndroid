package com.linku.im.screen.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.domain.Auth
import com.linku.im.Constants
import com.linku.im.linku.LinkUEvent
import com.linku.im.screen.chat.composable.ChatBottomBar
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.vm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state
    val listState = rememberLazyListState()

    vm.onActions {

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
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp,
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
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
            ChatBottomBar(
                text = state.text,
                firstVisibleItemIndex = firstVisibleItemIndex,
                listState = listState,
                onAction = { viewModel.onEvent(ChatEvent.ReadAll) },
                onSend = { viewModel.onEvent(ChatEvent.SendTextMessage) },
                onText = { viewModel.onEvent(ChatEvent.TextChange(it)) }
            )
        }
    }
    LaunchedEffect(state.scrollToBottom) {
        state.scrollToBottom.handle { listState.animateScrollToItem(0) }
    }
}