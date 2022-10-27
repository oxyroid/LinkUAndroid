package com.linku.im.screen.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.DarkMode
import androidx.compose.material.icons.sharp.LightMode
import androidx.compose.material.icons.sharp.Notifications
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bumble.appyx.navmodel.backstack.operation.push
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.appyx.target.NavTarget
import com.linku.im.ktx.runtime.ComposableLifecycle
import com.linku.im.ktx.runtime.rememberedRun
import com.linku.im.ktx.ui.graphics.animated
import com.linku.im.ktx.ui.graphics.times
import com.linku.im.ui.brush.premiumBrush
import com.linku.im.ui.components.BottomSheetContent
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.button.MaterialButton
import com.linku.im.ui.components.button.MaterialIconButton
import com.linku.im.ui.components.item.PinnedConversationItem
import com.linku.im.ui.components.notify.NotifyHolder
import com.linku.im.ui.theme.LocalBackStack
import com.linku.im.ui.theme.LocalDuration
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalPagerApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val scaffoldState = rememberScaffoldState()

    val theme = LocalTheme.current
    val backStack = LocalBackStack.current
    val linkedNode by viewModel.linkedNode
    val mode = linkedNode.value

    val feedback = LocalHapticFeedback.current

    val selections = rememberedRun(vm.readable.isDarkMode) {
        listOf(
            Selection.Button(
                resId = R.string.notification,
                icon = Icons.Sharp.Notifications,
                onClick = { viewModel.onEvent(MainEvent.Forward(MainMode.Notifications)) }
            ),
            Selection.Route(
                resId = R.string.settings,
                target = NavTarget.Introduce(-1),
                icon = Icons.Sharp.Settings
            ),
            Selection.Switch(
                resId = R.string.toggle_theme,
                value = this@rememberedRun,
                onIcon = Icons.Sharp.LightMode,
                offIcon = Icons.Sharp.DarkMode,
                onClick = {
                    vm.onEvent(LinkUEvent.ToggleDarkMode)
                },
                onLongClick = {
                    backStack.push(NavTarget.Setting.Theme)
                }
            )
        )
    }

    ComposableLifecycle { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            viewModel.onEvent(MainEvent.ObserveConversations)
        } else if (event == Lifecycle.Event.ON_STOP) {
            viewModel.onEvent(MainEvent.UnsubscribeConversations)
        }
    }

    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
        vm.message.handle { scaffoldState.snackbarHostState.showSnackbar(it) }
    }
    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            NotifyHolder(
                state = it,
                modifier = Modifier.fillMaxWidth()
            )
        },
        topBar = {
            ToolBar(
                navIcon = Icons.Rounded.Menu,
                onNavClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
                actions = {
                    MaterialIconButton(
                        icon = when (mode) {
                            is MainMode.Conversations -> Icons.Rounded.Search
                            is MainMode.Notifications -> Icons.Rounded.Close
                            is MainMode.NewChannel -> Icons.Rounded.Done
                        },
                        onClick = {
                            when (mode) {
                                MainMode.Conversations -> {
                                    backStack.push(NavTarget.Query)
                                }

                                MainMode.Notifications -> {
                                    viewModel.onEvent(MainEvent.Remain)
                                }

                                MainMode.NewChannel -> {

                                }
                            }
                        },
                        contentDescription = null
                    )
                },
                text = when (mode) {
                    is MainMode.Conversations -> globalLabelOrElse { stringResource(R.string.app_name) }
                    is MainMode.Notifications -> stringResource(R.string.notification)
                    is MainMode.NewChannel -> stringResource(R.string.new_channel)
                }
            )
        },
        backgroundColor = Color.Unspecified
    ) { innerPadding ->
        val pages = listOf(
            stringResource(R.string.tab_notification),
            stringResource(R.string.tab_contact),
            stringResource(R.string.tab_more)
        )
        Box {
            Column(
                modifier = Modifier
                    .background(theme.background.animated())
                    .padding(innerPadding)
            ) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier
                                .tabIndicatorOffset(
                                    tabPositions[pagerState.currentPage]
                                )
                                .padding(horizontal = LocalSpacing.current.large)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = LocalSpacing.current.extraSmall,
                                        topEnd = LocalSpacing.current.extraSmall
                                    )
                                ),
                            color = theme.primary.animated(),
                            height = LocalSpacing.current.extraSmall
                        )
                    },
                    divider = {},
                    tabs = {
                        pages.forEachIndexed { index, page ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                text = {
                                    Text(
                                        text = page,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                },
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(index) }
                                },
                                selectedContentColor = theme.onTopBar,
                                unselectedContentColor = theme.onTopBar * 0.6f,
                                modifier = Modifier
                                    .padding(LocalSpacing.current.small)
                                    .clip(RoundedCornerShape(LocalSpacing.current.extraSmall))
                            )
                        }
                    },
                    containerColor = theme.topBar.animated(),
                    contentColor = theme.onTopBar.animated()
                )
                HorizontalPager(
                    count = pages.size,
                    state = pagerState,
                    key = { it }
                ) { pageIndex ->
                    when (pageIndex) {
                        2 -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(LocalSpacing.current.small)
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
                                                        .size(LocalSpacing.current.small)
                                                        .background(
                                                            color = if (selection.value) Color.Green
                                                            else theme.error, shape = CircleShape
                                                        )
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(LocalSpacing.current.small)
                                            .clip(RoundedCornerShape(LocalSpacing.current.small))
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
                                            .padding(LocalSpacing.current.small)
                                            .clip(RoundedCornerShape(LocalSpacing.current.small))
                                            .background(brush)
                                            .clickable(
                                                role = Role.Button,
                                                onClick = {
                                                    vm.onEvent(LinkUEvent.Premium)
                                                }
                                            )
                                            .padding(LocalSpacing.current.medium)
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

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = !state.loadingConversations
                            ) {
                                val conversations = when (pageIndex) {
                                    0 -> state.conversations
                                    1 -> state.contracts
                                    else -> emptyList()
                                }
                                itemsIndexed(conversations) { index, conversation ->
                                    PinnedConversationItem(conversation = conversation,
                                        modifier = Modifier.animateItemPlacement(),
                                        onClick = {
                                            backStack.push(
                                                NavTarget.ChatTarget.Messages(
                                                    conversation.id
                                                )
                                            )
                                        },
                                        onLongClick = {
                                            viewModel.onEvent(MainEvent.Pin(conversation.id))
                                        })
                                    if (index != conversations.lastIndex) {
                                        Divider(
                                            color = LocalTheme.current.divider
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = LocalSpacing.current.medium,
                        vertical = LocalSpacing.current.large
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
                FloatingActionButton(
                    onClick = {
                        when (mode) {
                            MainMode.Conversations -> {
                                viewModel.onEvent(MainEvent.Forward(MainMode.NewChannel))
                            }

                            MainMode.NewChannel -> {

                            }

                            MainMode.Notifications -> {

                            }
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
                                        imageVector = Icons.Sharp.Add,
                                        contentDescription = null
                                    )
                                }
                            }

                            MainMode.NewChannel -> {
                                Column(
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(LocalSpacing.current.small)
                                ) {
                                    MaterialButton(
                                        text = "Next",
                                        modifier = Modifier.fillMaxWidth()
                                    ) {

                                    }
                                }
                            }
                        }
                    }
                }

            }

            BottomSheetContent(
                visible = mode is MainMode.Notifications,
                maxHeight = true,
                onDismiss = { viewModel.onEvent(MainEvent.Remain) }
            ) {
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
            }

        }
    }
    BackHandler(mode !is MainMode.Conversations) {
        viewModel.onEvent(MainEvent.Remain)
    }
}
