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
import androidx.compose.material.icons.sharp.ExpandCircleDown
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.material.icons.sharp.Reply
import androidx.compose.material.icons.sharp.Videocam
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.im.extension.ifTrue
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.chat.composable.ChatTextField
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
    val blurColor by animateColorAsState(if (state.focusMessageId != null) Color(0x88000000) else Color.Transparent)

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initialize(cid))
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

    val boxOffset = remember { mutableStateOf<IntSize?>(null) }
    ChatScaffold(
        type = state.type,
        title = state.title,
        blurColor = blurColor,
        navController = navController,
        listContent = {
            ListContent(
                loading = state.loading,
                messages = state.messages,
                listState = listState,
                onReply = {
                    viewModel.onEvent(ChatEvent.Reply(it))
                },
                onPreview = {
                    viewModel.onEvent(ChatEvent.ShowImage(it))
                },
                onProfile = {
                    navController.navigate(Screen.IntroduceScreen.withArgs(it))
                },
                onScroll = { position ->
                    scope.launch {
                        listState.scrollToItem(
                            index = position,
                            scrollOffset = boxOffset.value?.let { it.height / -2 } ?: 0
                        )
                    }
                },
                onFocus = { viewModel.onEvent(ChatEvent.FocusMessage(it)) },
                onFocusDismiss = { viewModel.onEvent(ChatEvent.LoseFocusMessage) },
                focusMessageId = state.focusMessageId,
                blurColor = blurColor,
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomSheetContent = {
            BottomSheetContent(
                modifier = Modifier.fillMaxWidth(),
                blurColor = blurColor,
                repliedMessage = state.repliedMessage,
                hasScrolled = firstVisibleItemIndex != 0,
                onDismissReply = { viewModel.onEvent(ChatEvent.Reply(null)) },
                onScrollToBottom = { scope.launch { listState.scrollToItem(0) } },
                dialogContent = {
                    PreviewDialog(state.uri) {
                        viewModel.onEvent(ChatEvent.OnFileUriChange(null))
                    }
                },
                snackHostContent = { SnackbarHost(hostState) },
                content = {
                    // Bottom Sheet
                    ChatTextField(
                        text = state.textFieldValue,
                        uri = state.uri,
                        emojis = state.emojis,
                        expended = state.expended,
                        blurColor = blurColor,
                        onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
                        onFile = { permissionState.launchPermissionRequest() },
                        onText = { viewModel.onEvent(ChatEvent.TextChange(it)) },
                        onEmoji = { viewModel.onEvent(ChatEvent.EmojiChange(it)) },
                        onExpanded = { viewModel.onEvent(ChatEvent.Expanded(!state.expended)) }
                    )
                }
            )
        },
        onListHeightChanged = { boxOffset.value = it }
    )

    OriginPreview(state.visitImage) { viewModel.onEvent(ChatEvent.DismissImage) }

    BackHandler(state.visitImage.isNotEmpty()) { viewModel.onEvent(ChatEvent.DismissImage) }

    BackHandler(state.focusMessageId != null) { viewModel.onEvent(ChatEvent.LoseFocusMessage) }

    LaunchedEffect(state.scroll) {
        state.scroll.handle {
            listState.scrollToItem(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScaffold(
    type: Conversation.Type,
    title: String,
    navController: NavHostController,
    blurColor: Color,
    listContent: @Composable BoxScope.() -> Unit,
    bottomSheetContent: @Composable BoxScope.() -> Unit,
    onListHeightChanged: (IntSize) -> Unit
) {
    Scaffold(
        topBar = {
            ToolBar(
                onNavClick = navController::navigateUp,
                actions = {
                    (type == Conversation.Type.PM).ifTrue {
                        MaterialIconButton(
                            icon = Icons.Sharp.Videocam,
                            onClick = { }
                        )
                    }
                },
                text = vm.readable.label ?: title,
                modifier = Modifier.drawWithContent {
                    drawContent()
                    drawRect(blurColor)
                }
            )
        },
        containerColor = LocalTheme.current.background,
        contentColor = LocalTheme.current.onBackground
    ) { innerPadding ->
        val chatBackgroundColor = LocalTheme.current.chatBackground
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawWithContent {
                    drawRect(chatBackgroundColor)
                    drawContent()
                }
                .onSizeChanged(onListHeightChanged),
            contentAlignment = Alignment.BottomCenter
        ) {
            listContent()
            bottomSheetContent()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ListContent(
    loading: Boolean,
    messages: List<MessageVO>,
    focusMessageId: Int?,
    blurColor: Color,
    listState: LazyListState,
    spacing: Dp = 150.dp,
    onReply: (Int) -> Unit,
    onPreview: (String) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit,
    onFocus: (Int) -> Unit,
    onFocusDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = spacing)
        ) {
            CircularProgressIndicator(
                color = LocalTheme.current.onPrimary
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawRect(blurColor)
                    drawContent()
                },
            reverseLayout = true,
            contentPadding = PaddingValues(
                vertical = LocalSpacing.current.extraSmall,
                horizontal = LocalSpacing.current.medium
            )
        ) {
            item {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(spacing)
                )
            }
            items(
                items = messages,
                key = { it.message.id }
            ) { messageVO ->
                val dismissState = rememberDismissState {
                    if (it == DismissValue.DismissedToStart) {
                        onReply(messageVO.message.id)
                    }
                    false
                }

                val config = messageVO.config
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(
                            top = 6.dp,
                            bottom = if (config.isEndOfGroup) 6.dp else 0.dp
                        )
                ) {
                    val message = messageVO.message
                    config.isShowTime.ifTrue {
                        Spacer(Modifier.height(LocalSpacing.current.extraSmall))
                        ChatTimestamp(
                            timestamp = message.timestamp,
                            blurColor = blurColor
                        )
                        Spacer(Modifier.height(LocalSpacing.current.extraSmall))
                    }
                    SwipeToDismiss(
                        state = dismissState,
                        background = { },
                        directions = setOf(
                            DismissDirection.EndToStart
                        )
                    ) {
                        ChatBubble(
                            message = message,
                            config = messageVO.config,
                            hasFocus = focusMessageId == message.id,
                            isOtherMessageHasFocus = focusMessageId != null && focusMessageId != message.id,
                            onPreview = onPreview,
                            onProfile = onProfile,
                            onScroll = onScroll,
                            onFocus = onFocus,
                            onFocusDismiss = onFocusDismiss,
                            blurColor = blurColor
                        )
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BottomSheetContent(
    repliedMessage: Message?,
    hasScrolled: Boolean,
    blurColor: Color,
    onDismissReply: () -> Unit,
    onScrollToBottom: () -> Unit,
    snackHostContent: @Composable () -> Unit,
    dialogContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fab and snackbar
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(
                    horizontal = LocalSpacing.current.medium
                )
        ) {
            AnimatedVisibility(
                visible = hasScrolled,
                enter = scaleIn(),
                exit = scaleOut(),
                modifier = Modifier
                    .padding(end = LocalSpacing.current.small)
            ) {
                SmallFloatingActionButton(
                    onClick = onScrollToBottom,
                    content = {
                        Icon(
                            imageVector = Icons.Sharp.ExpandMore,
                            contentDescription = null,
                            tint = LocalTheme.current.onPrimary
                        )
                    },
                    containerColor = LocalTheme.current.primary,
                    contentColor = LocalTheme.current.onPrimary,
                    elevation = FloatingActionButtonDefaults.loweredElevation(),
                    modifier = Modifier
                        .drawWithContent {
                            drawContent()
                            drawRect(blurColor)
                        }
                )
            }

            AnimatedVisibility(
                visible = repliedMessage != null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                SmallFloatingActionButton(
                    onClick = onDismissReply,
                    content = {
                        Icon(
                            imageVector = Icons.Sharp.Reply,
                            contentDescription = null,
                            tint = LocalTheme.current.onPrimary
                        )
                    },
                    containerColor = LocalTheme.current.primary,
                    contentColor = LocalTheme.current.onPrimary,
                    elevation = FloatingActionButtonDefaults.loweredElevation(),
                    modifier = Modifier
                        .drawWithContent {
                            drawContent()
                            drawRect(blurColor)
                        }
                )
            }
        }
        snackHostContent()
        // Image Preview Dialog
        dialogContent()
        content()
    }
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
                state = rememberDraggableState { if (it > 20) onDismissDialog() },
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
            border = BorderStroke(1.dp, LocalTheme.current.divider)
        ) {
            Box {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4 / 3f),
                    contentScale = ContentScale.Crop
                )

                MaterialIconButton(
                    icon = Icons.Sharp.ExpandCircleDown,
                    onClick = { onDismissDialog() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentDescription = null
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
            modifier = Modifier.background(LocalTheme.current.background)
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
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}