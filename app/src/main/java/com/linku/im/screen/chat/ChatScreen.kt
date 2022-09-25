package com.linku.im.screen.chat

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ExpandCircleDown
import androidx.compose.material.icons.sharp.ExpandMore
import androidx.compose.material.icons.sharp.Reply
import androidx.compose.material.ripple.LocalRippleTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.domain.entity.*
import com.linku.domain.struct.node.*
import com.linku.im.R
import com.linku.im.appyx.NavTarget
import com.linku.im.ktx.compose.foundation.lazy.isAtTop
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ktx.dsl.any
import com.linku.im.ktx.ifTrue
import com.linku.im.screen.chat.composable.*
import com.linku.im.screen.chat.vo.MessageVO
import com.linku.im.ui.components.*
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    cid: Int
) {
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val navController = LocalBackStack.current
    val hostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = state.firstVisibleIndex,
        initialFirstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset
    )
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onEvent(ChatEvent.OnFile(uri))
    }
    val permissionState = rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
        it.ifTrue { launcher.launch("image/*") }
    }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ChatEvent.Initialize(cid))
        if (cid == -1) navController.pop()
    }
    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            hostState.showSnackbar(it)
        }
        vm.message.handle {
            hostState.showSnackbar(it)
        }
    }

    val messages by viewModel.messageFlow.collectAsState(emptyList())
    val firstVisibleItemIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val offset by remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    LaunchedEffect(firstVisibleItemIndex, offset) {
        viewModel.onEvent(ChatEvent.OnScroll(firstVisibleItemIndex, offset))
    }

    LaunchedEffect(vm.readable.hasSynced) {
        if (vm.readable.hasSynced) {
            viewModel.onEvent(ChatEvent.Syncing)
        }
    }

    val linkedNode = viewModel.linkedNode.value
    val mode = linkedNode.value

    @Composable
    fun ChatScaffold(
        topBar: @Composable () -> Unit,
        listContent: @Composable BoxScope.() -> Unit,
        bottomSheetContent: @Composable BoxScope.() -> Unit,
        channelDetailContent: @Composable () -> Unit,
        imageDetailContent: @Composable (String, Rect, Float) -> Unit,
    ) {
        Scaffold(
            topBar = topBar,
            backgroundColor = LocalTheme.current.surface,
            contentColor = LocalTheme.current.onSurface
        ) { innerPadding ->
            Wrapper {
                val chatBackgroundColor = LocalTheme.current.chatBackground
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawRect(chatBackgroundColor)
                            drawContent()
                        }
                        .padding(innerPadding)
                        .navigationBarsPadding()
                        .imePadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Wrapper {
                        listContent()
                    }
                    Log.i("Recomposition", "ListContent")
                    Wrapper {
                        bottomSheetContent()
                    }
                }
            }
            if (mode is ChatScreenMode.ChannelDetail) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(LocalTheme.current.background)
                        .navigationBarsPadding()
                        .padding(innerPadding)
                ) {
                    channelDetailContent()
                }
            }

        }

        if (mode is ChatScreenMode.ImageDetail) {
            imageDetailContent(mode.url, mode.boundaries, mode.aspectRatio)
        }
    }

    ChatScaffold(
        topBar = {
            ChatTopBar(
                modeProvider = { mode },
                title = state.title,
                subTitle = vm.readable.label ?: state.subTitle,
                introduce = "",
                tonalElevation = when (mode) {
                    ChatScreenMode.Messages -> LocalSpacing.current.small
                    else -> 0.dp
                },
                onClick = { mode ->
                    when (mode) {
                        ChatScreenMode.Messages -> {
                            viewModel.onEvent(ChatEvent.Forward(ChatScreenMode.ChannelDetail))
                        }
                        ChatScreenMode.ChannelDetail -> {}
                        is ChatScreenMode.MemberDetail -> {}
                        is ChatScreenMode.ImageDetail -> {}
                    }
                },
                onNavClick = { mode ->
                    when (mode) {
                        ChatScreenMode.Messages -> navController.pop()
                        else -> viewModel.onEvent(ChatEvent.Remain)
                    }
                }
            )
        },
        listContent = {
            ListContent(
                listState = listState,
                loading = { !vm.readable.hasSynced },
                messages = messages,
                focusMessageIdProvider = { state.focusMessageId },
                onReply = { viewModel.onEvent(ChatEvent.OnReply(it)) },
                onResend = { viewModel.onEvent(ChatEvent.ResendMessage(it)) },
                onCancel = { viewModel.onEvent(ChatEvent.CancelMessage(it)) },
                onImagePreview = { mid, boundaries ->
                    viewModel.onEvent(
                        ChatEvent.Forward(
                            ChatScreenMode.ImageDetail(
                                mid,
                                boundaries
                            )
                        )
                    )
                },
                onProfile = {
                    navController.push(NavTarget.Introduce(it))
                },
                onScroll = { position ->
                    scope.launch {
                        listState.scrollToItem(position)
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
                hasScrolled = !listState.isAtTop,
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
                        text = { state.textFieldValue },
                        uri = { state.uri },
                        emojis = state.emojis,
                        emojiSpanExpanded = { state.emojiSpanExpanded },
                        onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
                        onFile = { permissionState.launchPermissionRequest() },
                        onText = { viewModel.onEvent(ChatEvent.OnTextChange(it)) },
                        onEmoji = { viewModel.onEvent(ChatEvent.OnEmoji(it)) },
                        onExpanded = { viewModel.onEvent(ChatEvent.OnEmojiSpanExpanded(!state.emojiSpanExpanded)) }
                    )
                }
            )
        },
        channelDetailContent = {
            LaunchedEffect(mode) {
                if (mode == ChatScreenMode.ChannelDetail) {
                    viewModel.onEvent(ChatEvent.FetchChannelDetail)
                }
            }
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.channelDetailLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val members by viewModel.memberFlow.collectAsState(emptyList())
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(members) {
                            MemberItem(member = it) {
                                val userId = it.uid
                                navController.push(
                                    NavTarget.Introduce(userId)
                                )
                            }
                        }
                    }
                }

                MaterialTextButton(
                    enabled = !viewModel.readable.shortcutPushed,
                    textRes = when {
                        viewModel.readable.shortcutPushed -> R.string.shortcutPushed
                        viewModel.readable.shortcutPushing -> R.string.shortcutPushing
                        else -> R.string.channel_add_to_desktop
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LocalSpacing.current.medium)
                ) {
                    viewModel.onEvent(ChatEvent.PushShortcut)
                }
                MaterialButton(
                    textRes = R.string.channel_exit,
                    containerColor = LocalTheme.current.error,
                    contentColor = LocalTheme.current.onError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LocalSpacing.current.medium)
                ) {

                }
            }
        },
        imageDetailContent = { model, boundaries, _ ->
            val context = LocalContext.current
            val request = remember(model) {
                ImageRequest.Builder(context).data(model).build()
            }
            val loader = remember(model) {
                ImageLoader.Builder(context).components {
                    add(ImageDecoderDecoder.Factory())
                }.build()
            }
            val painter = rememberAsyncImagePainter(
                model = request,
                imageLoader = loader
            )

            val paddingValues = WindowInsets.systemBars.asPaddingValues()
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current.density
            val topPadding = remember { paddingValues.calculateTopPadding().value * density }

            LocalRippleTheme.current.rippleAlpha().pressedAlpha
//            val sourceWidth = boundaries.width
//            val surfaceWidth = boundaries.width
//            val sourceHeight = sourceWidth * aspectRatio
//            val surfaceHeight = boundaries.height
//
//            val sourceWidthDp = (boundaries.width / ).dp
//            val surfaceWidthDp = boundaries.width.dp
//            val sourceHeightDp = sourceWidth.dp * aspectRatio
//            val surfaceHeightDp = boundaries.height.dp

            var isShowed by remember { mutableStateOf(false) }

            val duration = LocalDuration.current.medium
            var offsetY by remember { mutableStateOf(0f) }

            val transition = updateTransition(
                targetState = isShowed,
                label = "ImageDetail"
            )
            val animatedRadius by transition.animateInt(
                transitionSpec = { tween(duration) },
                label = "radius"
            ) {
                if (it) 0 else 5
            }
            val animatedOffset by transition.animateIntOffset(
                transitionSpec = { tween(duration) },
                label = "offset"
            ) {
                if (it) IntOffset.Zero
                else boundaries.topLeft.round()
            }
            val animatedWidthDp by transition.animateDp(
                transitionSpec = { tween(duration) },
                label = "width"
            ) {
                if (it) configuration.screenWidthDp.dp
                else (boundaries.width / density).dp
            }

            val animatedHeightDp by transition.animateDp(
                transitionSpec = { tween(duration) },
                label = "height"
            ) {
                if (it) configuration.screenHeightDp.dp
                else (boundaries.height / density).dp
            }

            val mDetailBackgroundAlpha by transition.animateFloat(
                transitionSpec = { tween(duration) },
                label = "backgroundAlpha"
            ) {
                if (it) 0.6f else 0f
            }
            val animatedTopPadding by transition.animateFloat(
                transitionSpec = { tween(duration) },
                label = "topPadding"
            ) {
                if (it) 0f else topPadding
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black * mDetailBackgroundAlpha)
                    .statusBarsPadding()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                viewModel.onEvent(ChatEvent.RemainIf {
                                    any {
                                        suggest { offsetY <= -configuration.screenHeightDp / 2f }
                                        suggest {
                                            (offsetY >= configuration.screenHeightDp / 2f).also {
                                                if (it) {
                                                    Log.e("Toast", "ChatScreen: offset down")
                                                }
                                            }
                                        }
                                    }
                                })

                                if (offsetY != 0f) offsetY = 0f
                            },
                            onVerticalDrag = { _, dragAmount ->
                                offsetY += dragAmount
                            }
                        )
                    }
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .offset {
                            animatedOffset + Offset(0f, offsetY - animatedTopPadding).round()
                        }
                        .size(
                            width = animatedWidthDp,
                            height = animatedHeightDp
                        )
                        .clip(RoundedCornerShape(animatedRadius)),
                    contentScale = ContentScale.Fit
                )
            }
            LaunchedEffect(mode, model, boundaries) {
                isShowed = mode is ChatScreenMode.ImageDetail
            }
        }
    )

    BackHandler(state.uri != null || state.focusMessageId != null || linkedNode.hasCache) {
        when {
            state.uri != null -> viewModel.onEvent(ChatEvent.OnFile(null))
            state.focusMessageId != null -> viewModel.onEvent(ChatEvent.OnFocus(null))
            linkedNode.hasCache -> viewModel.onEvent(ChatEvent.Remain)
        }
    }

    LaunchedEffect(state.scroll) {
        state.scroll.handle {
            listState.scrollToItem(it)
        }
    }
}


@Composable
private fun ListContent(
    listState: LazyListState,
    messages: List<MessageVO>,
    loading: () -> Boolean,
    focusMessageIdProvider: () -> Int?,
    onReply: (Int) -> Unit,
    onResend: (Int) -> Unit,
    onCancel: (Int) -> Unit,
    onImagePreview: (String, Rect) -> Unit,
    onProfile: (Int) -> Unit,
    onScroll: (Int) -> Unit,
    onFocus: (Int) -> Unit,
    onFocusDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    spacing: Dp = 150.dp
) {
    if (loading()) {
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
        val paddingValues = remember(messages) {
            messages.map {
                PaddingValues(
                    top = 6.dp,
                    bottom = if (it.config.isEndOfGroup) 6.dp else 0.dp
                )
            }
        }
        val isShowTimes = remember(messages) {
            messages.map { it.config.isShowTime }
        }
        val timestamps = remember(messages) {
            messages.map { it.message.timestamp }
        }
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
            itemsIndexed(messages) { index, it ->
                ChatBubble(
                    message = it.message,
                    focusIdProvider = focusMessageIdProvider,
                    configProvider = { it.config },
                    onImagePreview = onImagePreview,
                    onProfile = onProfile,
                    onScroll = onScroll,
                    onFocus = onFocus,
                    onDismissFocus = onFocusDismiss,
                    onReply = onReply,
                    onResend = onResend,
                    onCancel = onCancel,
                    modifier = Modifier.padding(paddingValues[index])
                )
                isShowTimes[index].ifTrue {
                    Log.i("Recomposition", "timestamp")
                    ChatTimestamp(
                        timestampProvider = { timestamps[index] },
                        modifier = Modifier
                            .padding(
                                vertical = LocalSpacing.current.extraSmall
                            )
                    )
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
                    modifier = Modifier.clip(FloatingActionButtonDefaults.smallShape)
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
                    modifier = Modifier.clip(FloatingActionButtonDefaults.smallShape)
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
                    onClick = { onDismiss() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentDescription = null
                )
            }
        }
    }
}