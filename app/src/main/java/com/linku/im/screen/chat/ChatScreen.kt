package com.linku.im.screen.chat

import android.Manifest
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.sharp.ExpandCircleDown
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.material.icons.sharp.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.domain.entity.Message
import com.linku.im.extension.ifTrue
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.composable.ChatBackground
import com.linku.im.screen.chat.composable.ChatBottomSheet
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.chat.composable.ChatTimestamp
import com.linku.im.screen.chat.vo.MessageVO
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalNavController
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    cid: Int
) {
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val hostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(ChatEvent.OnFileUriChange(uri))
    }
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        it.ifTrue { launcher.launch("image/*") }
    }
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initial(cid))
        if (cid == -1) navController.navigateUp()
    }
    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            hostState.showSnackbar(it)
        }
        vm.message.handle {
            hostState.showSnackbar(it)
        }
    }

    LaunchedEffect(firstVisibleItemIndex, offset) {
        viewModel.onEvent(ChatEvent.OnScroll(firstVisibleItemIndex, offset))
    }

    var boxOffset: IntSize? = null
    ChatScaffold(
        videoChatAllowed = state.videoChatAllowed,
        title = state.title,
        navController = navController,
        listContent = {
            ListContent(
                messages = state.messages,
                listState = listState,
                onReply = {
                    viewModel.onEvent(ChatEvent.Reply(it))
                },
                onPreview = {
                    viewModel.onEvent(ChatEvent.OnFileUriChange(Uri.parse(it)))
                },
                onProfile = {
                    navController.navigate(Screen.IntroduceScreen.withArgs(it))
                },
                onScroll = { position ->
                    scope.launch {
                        listState.animateScrollToItem(
                            index = position,
                            scrollOffset = boxOffset?.let { it.height / -2 } ?: 0
                        )
                    }
                }
            )
        },
        bottomSheetContent = {
            BottomSheetContent(
                repliedMessage = state.repliedMessage,
                onDismissReply = { viewModel.onEvent(ChatEvent.Reply(null)) },
                onScrollToBottom = { scope.launch { listState.scrollToItem(0) } },
                snackHostContent = { SnackbarHost(hostState) },
                textFieldContent = {
                    // Bottom Sheet
                    ChatBottomSheet(
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
                        modifier = Modifier.navigationBarsPadding()
                    )
                },
                imagePreviewDialog = {
                    PreviewDialog(uri = state.uri) {
                        viewModel.onEvent(ChatEvent.OnFileUriChange(null))
                    }
                }
            )
        },
        onListHeightChanged = {
            boxOffset = it
        }
    )

    OriginPreview(image = state.visitImage) {
        viewModel.onEvent(ChatEvent.DismissImage)
    }

    BackHandler(state.visitImage.isNotEmpty()) {
        viewModel.onEvent(ChatEvent.DismissImage)
    }
    LaunchedEffect(state.scroll) {
        state.scroll.handle {
            listState.animateScrollToItem(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScaffold(
    videoChatAllowed: Boolean,
    title: String,
    navController: NavHostController,
    listContent: @Composable () -> Unit,
    bottomSheetContent: @Composable () -> Unit,
    onListHeightChanged: (IntSize) -> Unit
) {
    Scaffold(
        topBar = {
            ToolBar(
                onNavClick = { navController.navigateUp() },
                actions = {
                    videoChatAllowed.ifTrue {
                        MaterialIconButton(
                            icon = Icons.Sharp.Videocam,
                            onClick = { },
                            contentDescription = "video"
                        )
                    }
                },
                text = vm.readable.label ?: title
            )
        },
        containerColor = LocalTheme.current.background,
        contentColor = LocalTheme.current.onBackground
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .onSizeChanged { onListHeightChanged(it) }
        ) {
            ChatBackground(Modifier.fillMaxSize())
            listContent()
            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                bottomSheetContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ListContent(
    messages: List<MessageVO>,
    listState: LazyListState,
    onReply: (Int) -> Unit,
    onPreview: (String) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(vertical = LocalSpacing.current.extraSmall)
    ) {
        item {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
        items(
            items = messages,
            key = { it.message.id }
        ) { messageVO ->
            val dismissState = rememberDismissState(confirmStateChange = {
                if (it == DismissValue.DismissedToStart) {
                    onReply(messageVO.message.id)
                }
                false
            })

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val config = messageVO.config
                val message = messageVO.message
                config.isShowTime.ifTrue {
                    Spacer(Modifier.height(LocalSpacing.current.extraSmall))
                    ChatTimestamp(message.timestamp)
                    Spacer(Modifier.height(LocalSpacing.current.extraSmall))
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
                        onPreview = { onPreview(it) },
                        onProfile = { onProfile(it) },
                        onScroll = { position -> onScroll(position) }
                    )
                }

            }
        }
    }
}

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
private fun BottomSheetContent(
    repliedMessage: Message?,
    onDismissReply: () -> Unit,
    onScrollToBottom: () -> Unit,
    snackHostContent: @Composable () -> Unit,
    imagePreviewDialog: @Composable () -> Unit,
    textFieldContent: @Composable () -> Unit
) {
    // Fab and snackbar
    Column(
        modifier = Modifier.animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = LocalSpacing.current.medium
                )
        ) {
            AnimatedVisibility(
                visible = repliedMessage != null,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .padding(end = LocalSpacing.current.medium)
            ) {
                ElevatedFilterChip(
                    selected = true,
                    onClick = { onDismissReply() },
                    label = {
                        Icon(
                            imageVector = Icons.Rounded.Reply,
                            contentDescription = ""
                        )
                    },
                    elevation = FilterChipDefaults.elevatedFilterChipElevation(
                        defaultElevation = 0.dp
                    )
                )
            }
            AnimatedVisibility(
                visible = repliedMessage != null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                SmallFloatingActionButton(
                    onClick = { onScrollToBottom() },
                    content = {
                        Icon(
                            imageVector = Icons.Sharp.ExpandMore,
                            contentDescription = ""
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    elevation = FloatingActionButtonDefaults.loweredElevation()
                )
            }
        }
        snackHostContent()
    }

    // Image Preview Dialog
    imagePreviewDialog()

    textFieldContent()
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PreviewDialog(
    uri: Uri?,
    onDismissDialog: () -> Unit
) {
    AnimatedContent(
        targetState = uri,
        modifier = Modifier
            .padding(LocalSpacing.current.extraSmall)
            .fillMaxWidth()
            .draggable(
                state = rememberDraggableState {
                    if (it > 20) onDismissDialog()
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

                MaterialIconButton(
                    icon = Icons.Sharp.ExpandCircleDown,
                    onClick = { onDismissDialog() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentDescription = "close"
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OriginPreview(
    image: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AnimatedVisibility(
        visible = image.isNotEmpty(),
        modifier = Modifier
            .fillMaxSize()
            .combinedClickable(
                onDoubleClick = onDismiss,
                onClick = {}
            )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {
            val model = ImageRequest.Builder(context).data(image).build()
            val loader = ImageLoader.Builder(context).components {
                add(ImageDecoderDecoder.Factory())
            }.build()
            val painter = rememberAsyncImagePainter(
                model = model, imageLoader = loader
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