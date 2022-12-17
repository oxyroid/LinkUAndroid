package com.linku.im.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumble.appyx.navmodel.backstack.operation.push
import com.bumble.appyx.navmodel.backstack.operation.singleTop
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.linku.core.extension.ifTrue
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.nav.target.NavTarget
import com.linku.im.ktx.runtime.LifecycleEffect
import com.linku.im.ktx.ui.graphics.animated
import com.linku.im.ktx.ui.graphics.times
import com.linku.im.screen.ConversationList
import com.linku.im.screen.MessageList
import com.linku.im.screen.UserList
import com.linku.im.screen.main.composable.ContactRequestItem
import com.linku.im.screen.main.composable.MainToolBar
import com.linku.im.ui.brush.premiumBrush
import com.linku.im.ui.components.BottomSheetContent
import com.linku.im.ui.components.MaterialTextField
import com.linku.im.ui.components.button.MaterialButton
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.components.item.ConversationItem
import com.linku.im.ui.components.item.PinnedContractsItem
import com.linku.im.ui.components.item.PinnedConversationItem
import com.linku.im.ui.components.item.UserItem
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalPagerApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    val theme = LocalTheme.current
    val spacing = LocalSpacing.current
    val backStack = LocalBackStack.current
    val feedback = LocalHapticFeedback.current

    val linkedNode by viewModel.linkedNode
    val mode = linkedNode.value

    val labels = MainStaticProvider.provideTabLabels(mode is MainMode.Query)

    val selections = MainStaticProvider.provideSelections(
        isDarkMode = vm.readable.isDarkMode,
        onNotification = {
            viewModel.onEvent(MainEvent.Forward(MainMode.Notifications))
        },
        onTheme = {
            backStack.push(NavTarget.Setting.Theme)
        },
        onGift = {

        },
        onToggleDarkMode = {
            vm.onEvent(LinkUEvent.ToggleDarkMode)
        }
    )

    LifecycleEffect { event ->
        if (event == Lifecycle.Event.ON_START) {
            viewModel.onEvent(MainEvent.ObserveConversations)
        } else if (event == Lifecycle.Event.ON_STOP) {
            viewModel.onEvent(MainEvent.UnsubscribeConversations)
        }
    }

    val topBarColor by animateColorAsState(
        when (mode) {
            is MainMode.Query -> theme.secondaryTopBar
            else -> theme.topBar
        }
    )
    val onTopBarColor by animateColorAsState(
        when (mode) {
            is MainMode.Query -> theme.onSecondaryTopBar
            else -> theme.onTopBar
        }
    )

    Scaffold(
        topBar = {
            MainToolBar(
                navIcon = when (mode) {
                    is MainMode.Query -> Icons.Rounded.ArrowBack
                    else -> Icons.Rounded.Menu
                },
                onNavClick = {
                    when (mode) {
                        is MainMode.Query -> {
                            viewModel.onEvent(MainEvent.Remain)
                        }
                        is MainMode.Conversations -> {
                            scope.launch {
                                pagerState.animateScrollToPage(labels.lastIndex)
                            }
                        }
                        else -> {}
                    }
                },
                actions = {
                    MaterialIconButton(
                        icon = when (mode) {
                            is MainMode.Conversations -> Icons.Rounded.Search
                            is MainMode.Notifications -> Icons.Rounded.Close
                            is MainMode.NewChannel -> Icons.Rounded.Done
                            is MainMode.Query -> Icons.Rounded.Search
                        },
                        onClick = {
                            when (mode) {
                                MainMode.Conversations -> {
                                    viewModel.onEvent(MainEvent.Forward(MainMode.Query))
                                }

                                MainMode.Notifications -> {
                                    viewModel.onEvent(MainEvent.Remain)
                                }

                                MainMode.NewChannel -> {

                                }

                                MainMode.Query -> {
                                    viewModel.onEvent(MainEvent.Query)
                                }
                            }
                        },
                        contentDescription = null
                    )
                },
                text = {
                    val text = when (mode) {
                        is MainMode.Conversations -> globalLabelOrElse { stringResource(R.string.app_name) }
                        is MainMode.Notifications -> stringResource(R.string.notification)
                        is MainMode.NewChannel -> stringResource(R.string.new_channel)
                        is MainMode.Query -> ""
                    }
                    val duration = text.isNotEmpty().ifTrue(800) ?: 0
                    Row {
                        when (mode is MainMode.Query) {
                            true -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    MaterialTextField(
                                        placeholder = stringResource(R.string.query_input),
                                        background = LocalTheme.current.surface,
                                        textFieldValue = state.queryText,
                                        onValueChange = {
                                            viewModel.onEvent(
                                                MainEvent.OnQueryText(it)
                                            )
                                        },
                                        imeAction = ImeAction.Search,
                                        keyboardActions = KeyboardActions(
                                            onSearch = {
                                                viewModel.onEvent(MainEvent.Query)
                                            }
                                        ),
                                        modifier = Modifier.padding(LocalSpacing.current.small)
                                    )

                                }
                            }
                            false -> {
                                val animation = remember {
                                    slideInVertically(tween(duration)) { it } +
                                            fadeIn(tween(duration)) with
                                            slideOutVertically(tween(duration)) { -it } +
                                            fadeOut(tween(duration))
                                }
                                AnimatedContent(
                                    targetState = text,
                                    transitionSpec = {
                                        animation.using(
                                            SizeTransform(true)
                                        )
                                    }
                                ) { target ->
                                    Text(
                                        text = target,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                backgroundColor = topBarColor,
                contentColor = onTopBarColor
            )
        },
        backgroundColor = Color.Unspecified,
        modifier = modifier
    ) { innerPadding ->
        Box {
            Column(
                modifier = Modifier
                    .background(theme.background.animated())
                    .padding(innerPadding)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier
                                .tabIndicatorOffset(
                                    tabPositions[pagerState.currentPage]
                                )
                                .padding(horizontal = spacing.large)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = spacing.extraSmall,
                                        topEnd = spacing.extraSmall
                                    )
                                ),
                            color = theme.primary.animated(),
                            height = spacing.extraSmall
                        )
                    },
                    divider = {},
                    tabs = {
                        labels.forEachIndexed { index, page ->
                            val selected = pagerState.currentPage == index
                            Tab(
                                selected = selected,
                                text = {
                                    Text(
                                        text = page,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                },
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                                selectedContentColor = onTopBarColor,
                                unselectedContentColor = onTopBarColor * 0.6f,
                                modifier = Modifier
                                    .padding(spacing.small)
                                    .clip(RoundedCornerShape(spacing.extraSmall))
                            )
                        }
                    },
                    containerColor = topBarColor,
                    contentColor = onTopBarColor
                )
                HorizontalPager(
                    count = labels.size,
                    state = pagerState,
                    key = { it }
                ) { pageIndex ->
                    when (PageConfig.parse(mode !is MainMode.Query, pageIndex)) {
                        PageConfig.Common.Main -> {

                        }
                        PageConfig.Common.Conversation -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = !state.loadingConversations
                            ) {
                                val conversations = state.conversations.value
                                val grouped = conversations.groupBy { it.pinned }

                                items(
                                    items = grouped[true] ?: emptyList(),
                                    key = { it.id }
                                ) { contact ->
                                    PinnedConversationItem(
                                        conversation = contact,
                                        onClick = {
                                            backStack.singleTop(
                                                NavTarget.ChatTarget.Messages(
                                                    contact.id
                                                )
                                            )
                                        },
                                        onLongClick = {
                                            viewModel.onEvent(MainEvent.Pin(contact.id))
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                                items(
                                    items = grouped[false] ?: emptyList(),
                                    key = { item -> item.id }
                                ) { conversation ->
                                    PinnedConversationItem(
                                        conversation = conversation,
                                        onClick = {
                                            backStack.singleTop(
                                                NavTarget.ChatTarget.Messages(
                                                    conversation.id
                                                )
                                            )
                                        },
                                        onLongClick = {
                                            viewModel.onEvent(MainEvent.Pin(conversation.id))
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                    Divider(color = theme.divider)
                                }
                            }
                        }
                        PageConfig.Common.Contract -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = !state.loadingConversations
                            ) {
                                val contracts = state.contracts.value
                                val grouped = contracts.groupBy { it.pinned }

                                items(
                                    items = grouped[true] ?: emptyList(),
                                    key = { it.id }
                                ) { contact ->
                                    PinnedContractsItem(
                                        contact = contact,
                                        onClick = {
                                            backStack.singleTop(
                                                NavTarget.ChatTarget.Messages(
                                                    contact.id
                                                )
                                            )
                                        },
                                        onLongClick = {
                                            viewModel.onEvent(MainEvent.Pin(contact.id))
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                }
                                items(
                                    items = grouped[false] ?: emptyList(),
                                    key = { it.id }
                                ) { contact ->
                                    PinnedContractsItem(
                                        contact = contact,
                                        onClick = {
                                            backStack.singleTop(
                                                NavTarget.ChatTarget.Messages(
                                                    contact.id
                                                )
                                            )
                                        },
                                        onLongClick = {
                                            viewModel.onEvent(MainEvent.Pin(contact.id))
                                        },
                                        modifier = Modifier.animateItemPlacement()
                                    )
                                    Divider(color = theme.divider)
                                }
                            }
                        }
                        PageConfig.Common.More -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(spacing.small)
                            ) {
                                items(selections) { selection ->
                                    ListItem(
                                        leadingContent = {
                                            Crossfade(selection.icon) { icon ->
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = stringResource(selection.resId)
                                                )
                                            }
                                        },
                                        headlineText = {
                                            Text(
                                                text = stringResource(selection.resId),
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        },
                                        trailingContent = {
                                            if (selection is Selection.Switch) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(spacing.small)
                                                        .background(
                                                            color = if (selection.value) Color.Green
                                                            else theme.error, shape = CircleShape
                                                        )
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(spacing.small)
                                            .clip(RoundedCornerShape(spacing.small))
                                            .combinedClickable(
                                                onClick = {
                                                    when (selection) {
                                                        is Selection.Route -> {
                                                            when (selection.target) {
                                                                is NavTarget.Introduce -> {
                                                                    backStack.push(vm.authenticator.currentUID?.let {
                                                                        NavTarget.Introduce(it)
                                                                    } ?: NavTarget.Sign)
                                                                }

                                                                else -> backStack.push(selection.target)
                                                            }
                                                        }

                                                        is Selection.Button -> selection.onClick()
                                                        is Selection.Switch -> selection.onClick()
                                                    }
                                                },
                                                onLongClick = {
                                                    if (selection !is Selection.Route) {
                                                        feedback.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                    }
                                                    when (selection) {
                                                        is Selection.Button -> selection.onLongClick()
                                                        is Selection.Switch -> selection.onLongClick()
                                                        else -> {}
                                                    }
                                                }
                                            ),
                                        colors = ListItemDefaults.colors(
                                            containerColor = theme.surface.animated(),
                                            leadingIconColor = (theme.onSurface * 0.6f).animated(),
                                            trailingIconColor = (theme.onSurface * 0.6f).animated(),
                                            headlineColor = theme.onSurface.animated()
                                        )
                                    )
                                }
                                item {
                                    val brush = premiumBrush()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .padding(spacing.small)
                                            .clip(RoundedCornerShape(spacing.small))
                                            .background(brush)
                                            .clickable(
                                                role = Role.Button,
                                                onClick = {
                                                    vm.onEvent(LinkUEvent.Premium)
                                                }
                                            )
                                            .padding(spacing.medium)
                                    ) {
                                        CompositionLocalProvider(
                                            LocalContentColor provides Color.White
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.WorkspacePremium,
                                                contentDescription = stringResource(R.string.premium),
                                                modifier = Modifier.padding(end = 16.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.premium),
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        PageConfig.Query.Conversation -> ConversationsQueryList(state.queryResultConversations)
                        PageConfig.Query.User -> UsersQueryList(state.queryResultUsers)
                        PageConfig.Query.Message -> MessagesQueryList(state.queryResultMessages)
                    }
                }
            }

            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = spacing.medium,
                        vertical = spacing.large
                    )
            ) {
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current.density
                val width by animateDpAsState(
                    if (mode is MainMode.NewChannel) (configuration.screenWidthDp * density).dp * 0.8f
                    else 56.dp,
                    tween(LocalDuration.current.medium)
                )
                val height by animateDpAsState(
                    if (mode is MainMode.NewChannel) (configuration.screenHeightDp * density).dp * 0.8f
                    else 56.dp,
                    tween(LocalDuration.current.medium)
                )
                val fabColor by animateColorAsState(
                    if (mode is MainMode.NewChannel) theme.background
                    else theme.primary,
                    tween(LocalDuration.current.medium)
                )
                val fabContentColor by animateColorAsState(
                    if (mode is MainMode.NewChannel) theme.onBackground
                    else theme.onPrimary,
                    tween(LocalDuration.current.medium)
                )
                AnimatedVisibility(
                    visible = mode != MainMode.Query,
                    enter = scaleIn(),
                    exit = scaleOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            when (mode) {
                                MainMode.Conversations -> {
                                    viewModel.onEvent(MainEvent.Forward(MainMode.NewChannel))
                                }
                                MainMode.NewChannel -> {}
                                MainMode.Notifications -> {}
                                MainMode.Query -> {}
                            }
                        },
                        elevation = FloatingActionButtonDefaults.loweredElevation(),
                        containerColor = fabColor,
                        contentColor = fabContentColor,
                        modifier = Modifier.size(
                            width = width,
                            height = height
                        )
                    ) {
                        Crossfade(mode) { mode ->
                            when (mode) {
                                MainMode.Conversations, MainMode.Notifications -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null
                                        )
                                    }
                                }

                                MainMode.NewChannel -> {
                                    Column(
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(spacing.small)
                                    ) {
                                        MaterialButton(
                                            text = "Next",
                                            modifier = Modifier.fillMaxWidth()
                                        ) {

                                        }
                                    }
                                }

                                MainMode.Query -> {}
                            }
                        }
                    }

                }

            }
            val isNotification = mode is MainMode.Notifications
            BottomSheetContent(
                visible = isNotification,
                maxHeight = true,
                onDismiss = { viewModel.onEvent(MainEvent.Remain) },
                content = {
                    LaunchedEffect(isNotification) {
                        if (isNotification) {
                            viewModel.onEvent(MainEvent.FetchNotifications)
                        }
                    }
                    val requests = state.requests.value
                    if (requests.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.CenterHorizontally)

                        ) {
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(R.raw.lottie_empty_box)
                            )
                            LottieAnimation(
                                composition = composition,
                                modifier = Modifier
                                    .size(160.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(spacing.small)
                        ) {
                            items(requests) {
                                ContactRequestItem(
                                    request = it,
                                    onClick = {
                                        backStack.push(NavTarget.Introduce(it.uid))
                                    }
                                )
                            }
                        }
                    }
                }
            )

        }
    }
    BackHandler(mode !is MainMode.Conversations) {
        viewModel.onEvent(MainEvent.Remain)
    }
}

@Composable
private fun ConversationsQueryList(
    conversations: ConversationList
) {
    val theme = LocalTheme.current
    val backStack = LocalBackStack.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = theme.background,
            )
    ) {
        items(conversations.value) { conversation ->
            ConversationItem(conversation = conversation) {
                backStack.push(
                    NavTarget.ChatTarget.Messages(
                        conversation.id
                    )
                )
            }
        }
    }
}

@Composable
private fun UsersQueryList(
    users: UserList
) {
    val theme = LocalTheme.current
    val backStack = LocalBackStack.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = theme.background,
            )
    ) {
        items(users.value) { user ->
            UserItem(user = user) {
                backStack.push(NavTarget.Introduce(user.id))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesQueryList(
    messages: MessageList
) {
    val theme = LocalTheme.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = theme.background,
            )
    ) {
        items(messages.value) { message ->
            ListItem(
                headlineText = {
                    Text(text = message.content)
                }
            )
        }
    }
}
