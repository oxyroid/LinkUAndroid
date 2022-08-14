package com.linku.im.screen.chat

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowCircleDown
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.material.icons.sharp.SwipeDown
import androidx.compose.material.icons.sharp.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.extension.friendlyFormatted
import com.linku.im.extension.ifTrue
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.composable.ChatBottomBar
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.ToolBarAction
import com.linku.im.vm
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class, ExperimentalFoundationApi::class
)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
) {
    val state = viewModel.readable
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val hostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(ChatEvent.OnFileUriChange(uri))
    }
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) {
            it.ifTrue { launcher.launch("image/*") }
        }
    LaunchedEffect(Unit) {
        if (cid == -1) vm.onEvent(LinkUEvent.PopBackStack)
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initial(cid))
    }
    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            hostState.showSnackbar(it)
        }
        vm.message.handle {
            hostState.showSnackbar(it)
        }
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
                actions = {
                    ToolBarAction(
                        onClick = { /*TODO*/ },
                        imageVector = Icons.Sharp.Videocam
                    )
                },
                text = state.title
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        var boxOffset: IntSize? = null
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .onSizeChanged {
                    boxOffset = it
                }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                itemsIndexed(
                    items = state.messages,
                    key = { _, vo -> vo.message.id }
                ) { index, messageVO ->
                    val dismissState = rememberDismissState(
                        confirmStateChange = {
                            if (it == DismissValue.DismissedToStart) {
                                viewModel.onEvent(ChatEvent.Reply(messageVO.message.id))
                            }
                            false
                        }
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val config = messageVO.config
                        val message = messageVO.message
                        config.isShowTime.ifTrue {
                            Text(
                                text = message.timestamp.friendlyFormatted,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        SwipeToDismiss(
                            state = dismissState,
                            background = {},
                            directions = setOf(
                                DismissDirection.EndToStart
                            )
                        ) {
                            ChatBubble(
                                message = messageVO.message,
                                config = messageVO.config,
                                onPreview = {
                                    viewModel.onEvent(ChatEvent.ShowImage(it))
                                },
                                onProfile = {
                                    vm.onEvent(
                                        LinkUEvent.NavigateWithArgs(
                                            Screen.IntroduceScreen.withArgs(it)
                                        )
                                    )
                                },
                                onScroll = {
                                    scope.launch {
                                        listState.animateScrollToItem(
                                            it,
                                            (boxOffset?.height?.div(-2)) ?: 0
                                        )
                                    }
                                }
                            )
                        }

                        if (index == 0) Spacer(Modifier.height(120.dp))
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedVisibility(
                    visible = firstVisibleItemIndex != 0,
                    enter = scaleIn(),
                    exit = scaleOut(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = {
                            scope.launch {
                                listState.scrollToItem(0)
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Sharp.ExpandMore,
                                contentDescription = ""
                            )
                        }
                    )
                }
                SnackbarHost(hostState)
                AnimatedContent(
                    targetState = state.uri,
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .draggable(
                            state = rememberDraggableState {
                                if (it > 20) viewModel.onEvent(ChatEvent.OnFileUriChange(null))
                            }, orientation = Orientation.Vertical
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
                    text = state.textFieldValue,
                    uri = state.uri,
                    emojis = state.emojis,
                    expended = state.expended,
                    onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
                    onFile = { permissionState.launchPermissionRequest() },
                    onText = {
                        viewModel.onEvent(ChatEvent.TextChange(it))
                    },
                    onEmoji = { viewModel.onEvent(ChatEvent.EmojiChange(it)) },
                    onExpanded = { viewModel.onEvent(ChatEvent.Expanded(!state.expended)) },
                    repliedMessage = state.repliedMessage
                )
            }
        }

        AnimatedVisibility(
            visible = state.visitImage.isNotEmpty(),
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onDoubleClick = { viewModel.onEvent(ChatEvent.DismissImage) },
                    onClick = {}
                )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) {
                val model = ImageRequest.Builder(context)
                    .data(state.visitImage)
                    .build()
                val loader = ImageLoader.Builder(context)
                    .components {
                        add(ImageDecoderDecoder.Factory())
                    }
                    .build()
                val painter = rememberAsyncImagePainter(
                    model = model,
                    imageLoader = loader
                )
                Image(
                    painter = painter,
                    contentDescription = "",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
    BackHandler(state.visitImage.isNotEmpty()) {
        viewModel.onEvent(ChatEvent.DismissImage)
    }
    LaunchedEffect(Unit) {
        viewModel.scrollEvent.collectLatest {
            listState.animateScrollToItem(0)
        }
    }
}