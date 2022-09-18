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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.domain.entity.Message
import com.linku.domain.struct.LinkedNode
import com.linku.domain.struct.hasNext
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.screen.chat.composable.ChatBubble
import com.linku.im.screen.chat.composable.ChatTextField
import com.linku.im.screen.chat.composable.ChatTimestamp
import com.linku.im.screen.chat.composable.ChatTopBar
import com.linku.im.screen.chat.vo.MessageVO
import com.linku.im.ui.components.MaterialButton
import com.linku.im.ui.components.MaterialIconButton
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
    val listState = rememberLazyListState()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(ChatEvent.OnFile(uri))
    }
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        it.ifTrue { launcher.launch("image/*") }
    }
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initialize(cid))
        if (cid == -1) navController.popBackStack()
    }
    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            hostState.showSnackbar(it)
        }
        vm.message.handle {
            hostState.showSnackbar(it)
        }
    }

    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    LaunchedEffect(firstVisibleItemIndex, offset) {
        viewModel.onEvent(ChatEvent.OnScroll(firstVisibleItemIndex, offset))
    }

    LaunchedEffect(vm.readable.isSyncingReady) {
        if (vm.readable.isSyncingReady) {
            viewModel.onEvent(ChatEvent.Syncing)
        }
    }

    val boxOffset = remember { mutableStateOf<IntSize?>(null) }

    var node: LinkedNode<ChatScreenMode> by remember {
        mutableStateOf(LinkedNode(ChatScreenMode.Messages))
    }

    ChatScaffold(
        node = node,
        topBar = {
            val label = remember(vm.readable.label, state.title) {
                vm.readable.label ?: state.title
            }
            ChatTopBar(
                label = label,
                node = node,
                onClick = { mode ->
                    when (mode) {
                        ChatScreenMode.Messages -> node =
                            LinkedNode(ChatScreenMode.ChannelDetail, node)
                        ChatScreenMode.ChannelDetail -> {}
                        is ChatScreenMode.MemberDetail -> {}
                        is ChatScreenMode.MessageDetail -> {}
                    }
                },
                onNavClick = { mode ->
                    when (mode) {
                        ChatScreenMode.Messages -> navController.popBackStack()
                        else -> {
                            node = node.next ?: node
                        }
                    }
                }
            )
        },
        listContent = {
            ListContent(
                loading = !vm.readable.isSyncingReady,
                listState = listState,
                messages = state.messages,
                focusMessageId = state.focusMessageId,
                onReply = { viewModel.onEvent(ChatEvent.OnReply(it)) },
                onResend = { viewModel.onEvent(ChatEvent.ResendMessage(it)) },
                onCancel = { viewModel.onEvent(ChatEvent.CancelMessage(it)) },
                onPreview = { url ->
                    viewModel.onEvent(ChatEvent.OnPreview(url))
                },
                onProfile = {
                    navController.navigate(
                        R.id.action_chatFragment_to_introduceFragment,
                        bundleOf("uid" to it)
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
                onFocus = { viewModel.onEvent(ChatEvent.OnFocus(it)) },
                onFocusDismiss = { viewModel.onEvent(ChatEvent.OnFocus(null)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomSheetContent = {
            BottomSheetContent(
                modifier = Modifier.fillMaxWidth(),
                repliedMessage = state.repliedMessage,
                hasScrolled = !isAtTop,
                onDismissReply = { viewModel.onEvent(ChatEvent.OnReply(null)) },
                onScrollToBottom = {
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                },
                dialogContent = {
                    PreviewDialog(state.uri) {
                        viewModel.onEvent(ChatEvent.OnFile(null))
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
                        onText = { viewModel.onEvent(ChatEvent.OnTextChange(it)) },
                        onEmoji = { viewModel.onEvent(ChatEvent.OnEmoji(it)) },
                        onExpanded = { viewModel.onEvent(ChatEvent.OnExpanded(!state.expended)) }
                    )
                }
            )
        },
        originPreview = {
            OriginPreview(preview = state.preview,
                onDismiss = { viewModel.onEvent(ChatEvent.OnPreview(null)) })
        },
        onListHeightChanged = { boxOffset.value = it },
        channelDetailContent = { innerPadding ->
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    LocalSpacing.current.extraSmall,
                    Alignment.Bottom
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = LocalSpacing.current.medium)
            ) {
                MaterialButton(
                    textRes = R.string.channel_add_to_desktop,
                    modifier = Modifier.fillMaxWidth()
                ) {

                }
                MaterialButton(
                    textRes = R.string.channel_exit,
                    containerColor = LocalTheme.current.error,
                    contentColor = LocalTheme.current.onError,
                    modifier = Modifier.fillMaxWidth()
                ) {

                }
            }
        }
    )

    BackHandler(state.preview != null) { viewModel.onEvent(ChatEvent.OnPreview(null)) }

    BackHandler(state.focusMessageId != null) { viewModel.onEvent(ChatEvent.OnFocus(null)) }

    BackHandler(node.hasNext) { node = node.next ?: node }

    LaunchedEffect(state.scroll) {
        state.scroll.handle {
            listState.scrollToItem(it)
        }
    }
}


@Composable
private fun ChatScaffold(
    node: LinkedNode<ChatScreenMode>,
    topBar: @Composable () -> Unit,
    listContent: @Composable BoxScope.() -> Unit,
    bottomSheetContent: @Composable BoxScope.() -> Unit,
    channelDetailContent: @Composable (PaddingValues) -> Unit,
    originPreview: @Composable BoxScope.() -> Unit,
    onListHeightChanged: (IntSize) -> Unit
) {
    Scaffold(
        topBar = topBar,
        backgroundColor = LocalTheme.current.background,
        contentColor = LocalTheme.current.onBackground,
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        Crossfade(node.value) { mode ->
            when (mode) {
                ChatScreenMode.Messages -> {
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
                            .onSizeChanged(onListHeightChanged),
                        contentAlignment = Alignment.BottomCenter) {
                        listContent()
                        bottomSheetContent()
                        originPreview()
                    }
                }
                ChatScreenMode.ChannelDetail -> Box(Modifier.fillMaxSize()) {
                    channelDetailContent(innerPadding)
                }
                is ChatScreenMode.MemberDetail -> TODO()
                is ChatScreenMode.MessageDetail -> TODO()
            }
        }
//        AnimatedContent(
//            targetState = node,
//            transitionSpec = {
//                fadeIn() + slideInHorizontally { it } with fadeOut() + slideOutHorizontally { it }
//            }
//        ) {
//            when (it.value) {
//                ChatScreenMode.Messages -> {
//                    val chatBackgroundColor = LocalTheme.current.chatBackground
//                    Box(
//                        Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding)
//                            .imePadding()
//                            .drawWithContent {
//                                drawRect(chatBackgroundColor)
//                                drawContent()
//                            }
//                            .onSizeChanged(onListHeightChanged),
//                        contentAlignment = Alignment.BottomCenter) {
//                        listContent()
//                        bottomSheetContent()
//                        originPreview()
//                    }
//                }
//                ChatScreenMode.ChannelDetail -> Box(Modifier.fillMaxSize()) {
//                    channelDetailContent(innerPadding)
//                }
//                is ChatScreenMode.MemberDetail -> TODO()
//                is ChatScreenMode.MessageDetail -> TODO()
//            }
//        }
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
    uri: Uri?,
    onDismiss: () -> Unit
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
    preview: String?,
    delay: Long = 120L,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
    val model = remember(preview) {
        ImageRequest.Builder(context).data(preview).build()
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
    LaunchedEffect(preview) {
        if (!preview.isNullOrEmpty()) {
            delay(delay)
            state = true
        }
    }
}