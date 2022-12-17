package com.linku.im.screen.chat

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandCircleDown
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.core.ktx.dsl.any
import com.linku.core.extension.ifTrue
import com.linku.core.utils.hasCache
import com.linku.domain.entity.Message
import com.linku.im.R
import com.linku.im.nav.target.NavTarget
import com.linku.im.ktx.foundation.lazy.isAtTop
import com.linku.im.ktx.runtime.LifecycleEffect
import com.linku.im.ktx.runtime.rememberedRun
import com.linku.im.ktx.ui.graphics.times
import com.linku.im.screen.MessageUIList
import com.linku.im.screen.chat.composable.*
import com.linku.im.screen.main.globalLabelOrElse
import com.linku.im.ui.components.*
import com.linku.im.ui.components.button.MaterialButton
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.components.button.MaterialTextButton
import com.linku.im.ui.components.item.MemberItem
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    cid: Int,
    modifier: Modifier = Modifier
) {
    val systemUiController = rememberSystemUiController()
    val theme = LocalTheme.current
    LifecycleEffect { event ->
        if (event == Lifecycle.Event.ON_CREATE) {
            systemUiController.setNavigationBarColor(
                color = theme.surface,
                darkIcons = !theme.isDarkText
            )
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = theme.isDarkText
            )
            viewModel.restore()
        }
    }
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val backStack = LocalBackStack.current
    val hostState = remember(::SnackbarHostState)
    val listState = rememberLazyListState()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            viewModel.onEvent(ChatEvent.OnFile(uri))
        }

    LaunchedEffect(cid) {
        if (cid != state.cid) {
            viewModel.onEvent(ChatEvent.ResetNode)
        }
    }
    LifecycleEffect { event ->
        if (event == Lifecycle.Event.ON_CREATE) {
            viewModel.onEvent(ChatEvent.FetchChannel(cid))
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            viewModel.restore()
        }
    }

    val messages by viewModel.messageFlow.collectAsState(MessageUIList())

    LaunchedEffect(vm.readable.readyForObserveMessages) {
        if (vm.readable.readyForObserveMessages) {
            viewModel.onEvent(ChatEvent.ObserveMessage)
        }
    }

    val linkedNode by viewModel.linkedNode
    val mode = linkedNode.value

    @Composable
    fun ChatScaffold(
        topBar: @Composable () -> Unit,
        listContent: @Composable BoxScope.() -> Unit,
        bottomSheetContent: @Composable BoxScope.() -> Unit,
        channelDetailContent: @Composable () -> Unit,
        imageDetailContent: @Composable (String, Rect, Float) -> Unit
    ) {
        Scaffold(
            topBar = topBar,
            backgroundColor = theme.surface,
            contentColor = theme.onSurface,
            modifier = modifier
        ) { innerPadding ->
            Wrapper {
                val chatBackgroundColor = theme.chatBackground
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawRect(chatBackgroundColor)
                            drawContent()
                        }
                        .padding(innerPadding)
                        .imePadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Wrapper {
                        listContent()
                    }
                    Wrapper {
                        bottomSheetContent()
                    }
                }
            }
            AnimatedVisibility(
                visible = mode is ChatMode.ChannelDetail,
                enter = slideInHorizontally { it },
                exit = slideOutHorizontally { it }
            ) {

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(theme.background)
                        .navigationBarsPadding()
                        .padding(innerPadding)
                ) {
                    channelDetailContent()
                }
            }
        }

        if (mode is ChatMode.ImageDetail) {
            imageDetailContent(
                mode.url,
                mode.boundaries,
                mode.aspectRatio
            )
        }
    }

    ChatScaffold(
        topBar = {
            ChatTopBar(
                modeProvider = { mode },
                title = state.title,
                subTitle = globalLabelOrElse { state.subTitle },
                introduce = "",
                tonalElevation = when (mode) {
                    ChatMode.Messages -> LocalSpacing.current.small
                    else -> 0.dp
                },
                onClick = { mode ->
                    when (mode) {
                        ChatMode.Messages -> {
                            viewModel.onEvent(ChatEvent.Forward(ChatMode.ChannelDetail))
                        }

                        ChatMode.ChannelDetail -> {}
                        is ChatMode.MemberDetail -> {}
                        is ChatMode.ImageDetail -> {}
                    }
                },
                onNavClick = { mode ->
                    when (mode) {
                        ChatMode.Messages -> backStack.pop()
                        else -> viewModel.onEvent(ChatEvent.Remain)
                    }
                }
            )
        },
        listContent = {
            val configuration = LocalConfiguration.current
            val height = configuration.screenHeightDp
            ListContent(
                listState = listState,
                loadingProvider = { !vm.readable.readyForObserveMessages },
                messages = messages,
                focusMessageIdProvider = { state.focusMessageId },
                onReply = { viewModel.onEvent(ChatEvent.OnReply(it)) },
                onResend = { viewModel.onEvent(ChatEvent.ResendMessage(it)) },
                onCancel = { viewModel.onEvent(ChatEvent.CancelMessage(it)) },
                onImagePreview = { mid, boundaries ->
                    viewModel.onEvent(
                        ChatEvent.Forward(
                            ChatMode.ImageDetail(
                                mid,
                                boundaries
                            )
                        )
                    )
                },
                onProfile = {
                    backStack.push(NavTarget.Introduce(it))
                },
                onScroll = { position ->
                    scope.launch {
                        listState.scrollToItem(
                            index = position,
                            scrollOffset = -height / 2
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
                hasScrolled = !listState.isAtTop,
                onDismissReply = { viewModel.onEvent(ChatEvent.OnReply(null)) },
                onScrollToBottom = {
                    scope.launch {
                        listState.scrollToItem(0)
                    }
                },
                dialogContent = {
                    PreviewDialog(
                        uri = state.uri,
                        onDismiss = {
                            viewModel.onEvent(ChatEvent.OnFile(null))
                        }
                    )
                },
                snackHostContent = { SnackbarHost(hostState) },
                content = {
                    ChatTextField(
                        text = state.textFieldValue,
                        uri = state.uri,
                        onSend = { viewModel.onEvent(ChatEvent.SendMessage) },
                        onFile = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        onText = { viewModel.onEvent(ChatEvent.OnTextChange(it)) },
                        onExpanded = { viewModel.onEvent(ChatEvent.OnEmojiSpanExpanded(!state.emojiSpanExpanded)) }
                    )
                    AnimatedVisibility(
                        visible = state.emojiSpanExpanded,
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(theme.surface),
                            columns = GridCells.Adaptive(48.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            items(state.emojis) {
                                EmojiButton(
                                    emoji = it,
                                    onClick = { viewModel.onEvent(ChatEvent.OnEmoji(it.emoji)) }
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier = Modifier
                            .background(theme.surface)
                            .navigationBarsPadding()
                    )
                }
            )
        },
        channelDetailContent = {
            LaunchedEffect(mode) {
                if (mode == ChatMode.ChannelDetail) {
                    viewModel.onEvent(ChatEvent.FetchChannelDetail)
                }
            }
            Column(
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = LocalSpacing.current.medium)
            ) {
                if (state.channelDetailLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    val members by viewModel.memberFlow.collectAsStateWithLifecycle(emptyList())
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(members) {
                            Column {
                                MemberItem(member = it) {
                                    val userId = it.uid
                                    backStack.push(
                                        NavTarget.Introduce(userId)
                                    )
                                }
                                Divider(
                                    thickness = 1.dp,
                                    color = theme.divider
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
                    containerColor = theme.error,
                    contentColor = theme.onError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LocalSpacing.current.medium)
                ) {

                }
            }
        },
        imageDetailContent = { model, boundaries, _ ->
            val context = LocalContext.current
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current.density
            val duration = LocalDuration.current.medium

            val paddingValues = WindowInsets.systemBars.asPaddingValues()

            val request = rememberedRun(model) {
                ImageRequest.Builder(context).data(this).build()
            }
            val loader = remember {
                ImageLoader.Builder(context)
                    .components {
                        add(ImageDecoderDecoder.Factory())
                    }
                    .build()
            }
            val painter = rememberAsyncImagePainter(
                model = request,
                imageLoader = loader
            )

            val topPadding = remember { paddingValues.calculateTopPadding().value * density }
            var isShowed by remember { mutableStateOf(false) }
            var offsetY by remember { mutableStateOf(0f) }
            val transition = updateTransition(
                targetState = isShowed,
                label = "ImageDetail"
            )
            val animatedRadius by transition.animateInt(
                transitionSpec = {
                    tween(
                        durationMillis = duration,
                        delayMillis = duration
                    )
                },
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
                isShowed = mode is ChatMode.ImageDetail
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
fun ListContent(
    listState: LazyListState,
    messages: MessageUIList,
    loadingProvider: () -> Boolean,
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
    if (loadingProvider()) {
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
        val paddingValues = rememberedRun(messages.value) {
            map {
                PaddingValues(
                    top = 6.dp,
                    bottom = if (it.config.isEndOfGroup) 6.dp else 0.dp
                )
            }
        }
        val isShowTimes = rememberedRun(messages.value) {
            map { it.config.isShowTime }
        }
        val timestamps = rememberedRun(messages.value) {
            map { it.message.timestamp }
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
            itemsIndexed(
                items = messages.value,
                key = { _, item -> item.message.id }
            ) { index, it ->
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
fun BottomSheetContent(
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
                            imageVector = Icons.Rounded.ExpandMore,
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
                            imageVector = Icons.Rounded.Reply,
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

@Composable
private fun PreviewDialog(
    uri: Uri?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    var offset by remember { mutableStateOf(0f) }
    val animateOffset by animateFloatAsState(offset)
    var pressed by remember { mutableStateOf(false) }
    val draggableState = rememberDraggableState { offset += it }

    val uriRemembered by rememberUpdatedState(uri)
    val onDismissRemembered by rememberUpdatedState(onDismiss)

    AnimatedVisibility(
        visible = uriRemembered != null,
        modifier = modifier
            .padding(LocalSpacing.current.extraSmall)
            .fillMaxWidth(),
        enter = slideInVertically { it * 2 },
        exit = slideOutVertically { it * 2 }
    ) {
        Surface(
            shape = RoundedCornerShape(5),
            border = BorderStroke(1.dp, LocalTheme.current.divider),
            modifier = Modifier
                .graphicsLayer {
                    translationY = animateOffset.coerceAtLeast(0f)
                }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = draggableState,
                    onDragStarted = {
                        pressed = true
                    },
                    onDragStopped = {
                        if (offset > configuration.screenHeightDp * density / 4) {
                            onDismissRemembered()
                        }
                        offset = 0f
                        pressed = false
                    },
                )
        ) {
            Box {
                var realUrl by remember {
                    mutableStateOf(Uri.EMPTY)
                }
                LaunchedEffect(Unit) {
                    if (uriRemembered != null) realUrl = uriRemembered
                }
                AsyncImage(
                    model = realUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4 / 3f),
                    contentScale = ContentScale.Crop
                )

                MaterialIconButton(
                    icon = Icons.Rounded.ExpandCircleDown,
                    onClick = { onDismissRemembered() },
                    modifier = Modifier.align(Alignment.TopEnd),
                    contentDescription = null
                )
            }
        }
    }
}
