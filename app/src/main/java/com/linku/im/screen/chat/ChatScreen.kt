package com.linku.im.screen.chat

import android.Manifest
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.chat.composable.ChatTextField
import com.linku.im.screen.chat.composable.ChatTimestamp
import com.linku.im.screen.chat.vo.MessageVO
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.components.NativeLazyList
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalNavController
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.delay
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
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = Int.MAX_VALUE
    )
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(ChatEvent.OnFileUriChange(uri))
    }
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        it.ifTrue { launcher.launch("image/*") }
    }
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

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

    LaunchedEffect(vm.readable.isSyncingReady) {
        if (vm.readable.isSyncingReady) {
            viewModel.onEvent(ChatEvent.Syncing)
        }
    }

    val boxOffset = remember { mutableStateOf<IntSize?>(null) }
    ChatScaffold(
        type = state.type,
        title = state.title,
        navController = navController,
        listContent = {
            ListContent(
                loading = !vm.readable.isSyncingReady,
                listState = listState,
                messages = state.messages,
                focusMessageId = state.focusMessageId,
                onReply = {
                    viewModel.onEvent(ChatEvent.Reply(it))
                },
                onResend = { viewModel.onEvent(ChatEvent.ResendMessage(it)) },
                onCancel = { viewModel.onEvent(ChatEvent.CancelMessage(it)) },
                onPreview = { s ->
                    viewModel.onEvent(ChatEvent.Preview(ChatState.Preview(s)))
                },
                onProfile = {
                    navController.navigate(
                        R.id.action_chatFragment_to_introduceFragment,
                        bundleOf(
                            "uid" to it
                        )
                    )
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
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomSheetContent = {
            BottomSheetContent(
                modifier = Modifier.fillMaxWidth(),
                repliedMessage = state.repliedMessage,
                hasScrolled = !isAtTop,
                onDismissReply = { viewModel.onEvent(ChatEvent.Reply(null)) },
                onScrollToBottom = {
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                },
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
                        onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
                        onFile = { permissionState.launchPermissionRequest() },
                        onText = { viewModel.onEvent(ChatEvent.TextChange(it)) },
                        onEmoji = { viewModel.onEvent(ChatEvent.EmojiChange(it)) },
                        onExpanded = { viewModel.onEvent(ChatEvent.Expanded(!state.expended)) }
                    )
                }
            )
        },
        originPreview = {
            OriginPreview(preview = state.preview,
                onDismiss = { viewModel.onEvent(ChatEvent.DismissPreview) })
        },
        onListHeightChanged = { boxOffset.value = it })

    BackHandler(state.preview != null) { viewModel.onEvent(ChatEvent.DismissPreview) }

    BackHandler(state.focusMessageId != null) { viewModel.onEvent(ChatEvent.LoseFocusMessage) }

    LaunchedEffect(state.scroll) {
        state.scroll.handle {
            listState.scrollToItem(it)
        }
    }
}

@Composable
private fun ChatScaffold(
    type: Conversation.Type,
    title: String,
    navController: NavController,
    listContent: @Composable BoxScope.() -> Unit,
    bottomSheetContent: @Composable BoxScope.() -> Unit,
    originPreview: @Composable BoxScope.() -> Unit,
    onListHeightChanged: (IntSize) -> Unit
) {
    Scaffold(
        topBar = {
            ToolBar(
                onNavClick = navController::navigateUp,
                actions = {
                    (type == Conversation.Type.PM).ifTrue {
                        MaterialIconButton(icon = Icons.Sharp.Videocam, onClick = { })
                    }
                },
                text = vm.readable.label ?: title
            )
        },
        backgroundColor = LocalTheme.current.background,
        contentColor = LocalTheme.current.onBackground,
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        val chatBackgroundColor = LocalTheme.current.chatBackground
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .drawWithContent {
                    drawRect(chatBackgroundColor)
                    drawContent()
                }
                .onSizeChanged(onListHeightChanged), contentAlignment = Alignment.BottomCenter) {
            listContent()
            bottomSheetContent()
            originPreview()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ListContent(
    loading: Boolean,
    listState: LazyListState,
    messages: List<MessageVO>,
    focusMessageId: Int?,
    onReply: (Int) -> Unit,
    onResend: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onPreview: (String) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit,
    onFocus: (Int) -> Unit,
    onFocusDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 150.dp
) {
    if (loading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = spacing)
        ) {
            CircularProgressIndicator(
                color = LocalTheme.current.onPrimary
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            reverseLayout = true
        ) {
            item {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(spacing)
                )
            }
            messages.forEach { current ->
                val config = current.config
                item {
                    ChatBubble(
                        message = current.message,
                        config = config,
                        hasFocus = focusMessageId == current.message.id,
                        lossFocus = focusMessageId != null && focusMessageId != current.message.id,
                        onPreview = onPreview,
                        onProfile = onProfile,
                        onScroll = onScroll,
                        onFocus = onFocus,
                        onDismissFocus = onFocusDismiss,
                        onReply = onReply,
                        onResend = onResend,
                        onCancel = onCancel,
                        modifier = Modifier.padding(
                            top = 6.dp,
                            bottom = if (config.isEndOfGroup) 6.dp else 0.dp
                        )
                    )
                }
                config.isShowTime.ifTrue {
                    stickyHeader {
                        val message = remember(current) {
                            current.message
                        }
                        Spacer(Modifier.height(LocalSpacing.current.extraSmall))
                        ChatTimestamp(timestamp = message.timestamp)
                        Spacer(Modifier.height(LocalSpacing.current.extraSmall))
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
    onDismissReply: () -> Unit,
    onScrollToBottom: () -> Unit,
    snackHostContent: @Composable () -> Unit,
    dialogContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    // Fab and snackbar
    Column(modifier) {
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
                modifier = Modifier.padding(end = LocalSpacing.current.small)
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
                        .clip(FloatingActionButtonDefaults.smallShape)
                )
            }

            AnimatedVisibility(
                visible = repliedMessage != null, enter = scaleIn(), exit = scaleOut()
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
                        .clip(FloatingActionButtonDefaults.smallShape)
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
    uri: Uri?, onDismiss: () -> Unit
) {
    AnimatedContent(
        targetState = uri,
        modifier = Modifier
            .padding(LocalSpacing.current.extraSmall)
            .fillMaxWidth()
            .draggable(
                state = rememberDraggableState { if (it > 20) onDismiss() },
                orientation = Orientation.Vertical
            ),
        transitionSpec = {
            slideInVertically { it } with slideOutVertically { -it }
        },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (it == null) return@AnimatedContent
        Surface(
            shape = RoundedCornerShape(5), border = BorderStroke(1.dp, LocalTheme.current.divider)
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
                    onClick = { onDismiss() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentDescription = null
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.OriginPreview(
    preview: ChatState.Preview?, delay: Long = 120L, onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val image = preview?.s

//    val configuration = LocalConfiguration.current
//    val screenDensity = configuration.densityDpi / 160f
//    val screenWidthDp: Int = (configuration.screenWidthDp.toFloat() * screenDensity).roundToInt()
//    val screenHeightDp: Int = (configuration.screenHeightDp.toFloat() * screenDensity).roundToInt()

    var state by remember { mutableStateOf(false) }
    val duration = remember { 400 }
    val transition = updateTransition(state, label = "transition")

//    val width by transition.animateDp(
//        label = "width-dp",
//        transitionSpec = { tween(duration) }
//    ) {
//        screenWidthDp.dp
//    }.also { Log.e("Measure", "OriginPreview: width=${it.value}") }
//
//    val height by transition.animateDp(
//        label = "height-dp",
//        transitionSpec = { tween(duration) }
//    ) {
//        if (!it) 0.dp else screenHeightDp.dp
//    }.also { Log.e("Measure", "OriginPreview: height=${it.value}") }

    val background by transition.animateColor(
        label = "color",
        transitionSpec = { tween(duration) }) {
        if (!it) Color.Transparent else LocalTheme.current.background
    }
    val model = remember(image) {
        ImageRequest.Builder(context).data(image).build()
    }
//    val loader = remember {
//        ImageLoader.Builder(context)
//            .components { add(ImageDecoderDecoder.Factory()) }
//            .build()
//    }
//    val painter = rememberAsyncImagePainter(
//        model = model,
//        imageLoader = loader
//    )
    AnimatedVisibility(visible = state,
        modifier = Modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .drawWithContent {
                drawRect(background)
                drawContent()
            }
            .combinedClickable(onDoubleClick = {
                onDismiss()
                state = false
            }, onClick = {})
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
    LaunchedEffect(image) {
        if (!image.isNullOrEmpty()) {
            delay(delay)
            state = true
        }
    }
}

@Composable
internal fun NativeListContent(
    messages: List<MessageVO>,
    focusMessageId: Int?,
    onReply: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onPreview: (String) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit,
    onFocus: (Int) -> Unit,
    onFocusDismiss: () -> Unit,
    onResend: (Int) -> Unit,
    modifier: Modifier
) {
    NativeLazyList(
        list = messages,
        item = { messageVO ->
            val message = remember(messageVO) {
                messageVO.message
            }
            val config = remember(messageVO) {
                messageVO.config
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(
                    top = 6.dp, bottom = if (config.isEndOfGroup) 6.dp else 0.dp
                )
            ) {
                config.isShowTime.ifTrue {
                    Spacer(Modifier.height(LocalSpacing.current.extraSmall))
                    ChatTimestamp(timestamp = message.timestamp)
                    Spacer(Modifier.height(LocalSpacing.current.extraSmall))
                }
                val replyDisabled = remember(config.sendState) {
                    config.sendState == Message.STATE_PENDING || config.sendState == Message.STATE_FAILED
                }
                ChatBubble(
                    message = message,
                    config = messageVO.config,
                    hasFocus = focusMessageId == message.id,
                    lossFocus = focusMessageId != null && focusMessageId != message.id,
                    onPreview = onPreview,
                    onProfile = onProfile,
                    onScroll = onScroll,
                    onFocus = onFocus,
                    onDismissFocus = onFocusDismiss,
                    onReply = onReply,
                    onResend = onResend,
                    onCancel = onCancel
                )
            }
        },
        layoutManager = LinearLayoutManager(
            LocalContext.current, LinearLayoutManager.VERTICAL, true
        ),
        isContentsTheSame = { old, new -> old.config == new.config },
        modifier = modifier.fillMaxSize()
    )
}