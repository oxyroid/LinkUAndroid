package com.linku.im.appyx.node

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.bumble.appyx.core.composable.Children
import com.bumble.appyx.core.composable.childrenAsState
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.linku.im.R
import com.linku.im.appyx.target.NavTarget
import com.linku.im.ktx.compose.foundation.lazy.isAtTop
import com.linku.im.ktx.compose.ui.graphics.times
import com.linku.im.ktx.dsl.any
import com.linku.im.ktx.ifTrue
import com.linku.im.screen.chat.*
import com.linku.im.screen.chat.composable.ChatTextField
import com.linku.im.screen.chat.composable.ChatTopBarAppyx
import com.linku.im.ui.components.MaterialButton
import com.linku.im.ui.components.MaterialTextButton
import com.linku.im.ui.components.MemberItem
import com.linku.im.ui.components.Wrapper
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@Deprecated("")
class ChatNode(
    private val cid: Int,
    buildContext: BuildContext,
    private val backStack: BackStack<NavTarget.ChatTarget> = BackStack(
        initialElement = NavTarget.ChatTarget.Messages(cid),
        savedStateMap = buildContext.savedStateMap
    )
) : ParentNode<NavTarget.ChatTarget>(
    buildContext = buildContext,
    navModel = backStack
) {
    private lateinit var viewModel: ChatViewModel
    private lateinit var state: ChatState
    private lateinit var stack: BackStack<NavTarget>

    @Composable
    override fun View(modifier: Modifier) {
        val children by backStack.childrenAsState()
        viewModel = (LocalContext.current as AppCompatActivity).viewModels<ChatViewModel>().value
        state = viewModel.readable
        stack = LocalBackStack.current
        Scaffold(
            topBar = {
                ChatTopBarAppyx(
                    targetProvider = { children.first().key.navTarget },
                    title = state.title,
                    subTitle = vm.readable.label ?: state.subTitle,
                    introduce = "",
                    tonalElevation = when (children.firstOrNull()?.key?.navTarget) {
                        is NavTarget.ChatTarget.Messages -> LocalSpacing.current.small
                        else -> 0.dp
                    },
                    onClick = { target ->
                        when (target) {
                            is NavTarget.ChatTarget.Messages -> {
                                backStack.push(NavTarget.ChatTarget.ChannelDetail(cid))
                            }

                            is NavTarget.ChatTarget.ChannelDetail -> {}
                            is NavTarget.ChatTarget.MemberDetail -> {}
                            is NavTarget.ChatTarget.ImageDetail -> {}
                        }
                    },
                    onNavClick = { target ->
                        when (target) {
                            is NavTarget.ChatTarget.Messages -> stack.pop()
                            else -> backStack.pop()
                        }
                    }
                )

            },
            backgroundColor = LocalTheme.current.surface,
            contentColor = LocalTheme.current.onSurface
        ) { innerPadding ->
            Children(
                navModel = backStack,
                transitionHandler = rememberBackstackSlider(
                    transitionSpec = { tween(LocalDuration.current.medium) }
                ),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    override fun resolve(navTarget: NavTarget.ChatTarget, buildContext: BuildContext): Node =
        node(buildContext) {
            val hostState = remember { SnackbarHostState() }
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = state.firstVisibleIndex,
                initialFirstVisibleItemScrollOffset = state.firstVisibleItemScrollOffset
            )
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    viewModel.onEvent(ChatEvent.OnFile(uri))
                }
            val permissionState =
                rememberPermissionState(Manifest.permission.READ_EXTERNAL_STORAGE) {
                    it.ifTrue { launcher.launch("image/*") }
                }

            val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START) {
                        viewModel.onEvent(ChatEvent.ObserveChannel(cid))
                    } else if (event == Lifecycle.Event.ON_STOP) {
                        viewModel.onEvent(ChatEvent.RemoveAllObservers)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
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

            LaunchedEffect(vm.readable.readyForObserveMessages) {
                if (vm.readable.readyForObserveMessages) {
                    viewModel.onEvent(ChatEvent.ObserveMessage)
                }
            }
            Wrapper {
                val chatBackgroundColor = LocalTheme.current.chatBackground
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            drawRect(chatBackgroundColor)
                            drawContent()
                        }
                        .navigationBarsPadding()
                        .imePadding(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val scope = rememberCoroutineScope()
                    Wrapper {
                        // List Content
                        ListContent(
                            listState = listState,
                            loading = { !vm.readable.readyForObserveMessages },
                            messages = messages,
                            focusMessageIdProvider = { state.focusMessageId },
                            onReply = { viewModel.onEvent(ChatEvent.OnReply(it)) },
                            onResend = { viewModel.onEvent(ChatEvent.ResendMessage(it)) },
                            onCancel = { viewModel.onEvent(ChatEvent.CancelMessage(it)) },
                            onImagePreview = { mid, boundaries ->
                                backStack.push(
                                    NavTarget.ChatTarget.ImageDetail(cid, mid, boundaries)
                                )
                            },
                            onProfile = {
                                stack.push(NavTarget.Introduce(it))
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

                    }
                    Wrapper {
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

                    }
                }
            }
            if (navTarget is NavTarget.ChatTarget.ChannelDetail) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(LocalTheme.current.background)
                        .navigationBarsPadding()
                ) {
                    LaunchedEffect(Unit) {
                        viewModel.onEvent(ChatEvent.FetchChannelDetail)
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
                                        stack.push(
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

                }
            }

            if (navTarget is NavTarget.ChatTarget.ImageDetail) {
                val (url, boundaries, aspectRatio) = with(navTarget) {
                    Triple(url, boundaries, aspectRatio)
                }
                val context = LocalContext.current
                val request = remember(url) {
                    ImageRequest.Builder(context).data(url).build()
                }
                val loader = remember(url) {
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
                LaunchedEffect(url, boundaries) {
                    isShowed = true
                }

            }

        }
}
