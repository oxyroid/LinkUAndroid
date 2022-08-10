package com.linku.im.screen.chat

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.extension.debug
import com.linku.im.extension.ifTrue
import com.linku.im.extension.toast
import com.linku.im.screen.chat.composable.ChatBottomBar
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.ui.components.ToolBar
import com.linku.im.vm

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
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

    SideEffect {
        if (cid == -1) vm.onEvent(LinkUEvent.PopBackStack)
    }
    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initial(cid))
    }
    LaunchedEffect(state.event) {
        state.event.handle(context::toast)
    }
    val firstVisibleItemIndex by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }
    val offset by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset
        }
    }
    LaunchedEffect(firstVisibleItemIndex, offset) {
        viewModel.onEvent(ChatEvent.OnScroll(firstVisibleItemIndex, offset))
    }

    Scaffold(
        topBar = {
            ToolBar(
                onNavClick = { vm.onEvent(LinkUEvent.PopBackStack) },
                actions = {},
                text = state.title
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
        ) {
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                reverseLayout = true,
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(state.messages) { index, it ->
                    Column {
                        ChatBubble(message = it.message, config = it.config)
                        if (index == 0) Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedContent(
                    targetState = state.uri,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .draggable(
                            state = rememberDraggableState {
                                if (it > 20) viewModel.onEvent(ChatEvent.OnFileUriChange(null))
                            },
                            orientation = Orientation.Vertical
                        ),
                    transitionSpec = {
                        slideInVertically { it } with slideOutVertically { -it }
                    },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (it == null) return@AnimatedContent
                    Surface(
                        shape = RoundedCornerShape(5),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box {
                            AsyncImage(
                                model = it,
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4 / 3f),
                                contentScale = ContentScale.Crop
                            )

                            IconButton(
                                onClick = { viewModel.onEvent(ChatEvent.OnFileUriChange(null)) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_expand_circle_down_24),
                                    contentDescription = "",
                                    tint = LocalContentColor.current
                                )
                            }
                        }
                    }
                }
                ChatBottomBar(
                    text = state.text,
                    uri = state.uri,
                    emojis = state.emojis,
                    expended = state.expended,
                    onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
                    onFile = { permissionState.launchPermissionRequest() },
                    onText = { viewModel.onEvent(ChatEvent.TextChange(it)) },
                    onEmoji = { viewModel.onEvent(ChatEvent.EmojiChange(it)) },
                    onExpanded = { viewModel.onEvent(ChatEvent.Expanded(!state.expended)) }
                )
            }
        }

    }

    val event by remember {
        derivedStateOf { state.scrollToBottomEvent }
    }
    var times by remember {
        mutableStateOf(0)
    }
    LaunchedEffect(event) {
        debug {
            context.toast("scroll: ${++times}")
        }
        event.handle {
            listState.animateScrollToItem(0)
        }
    }
}