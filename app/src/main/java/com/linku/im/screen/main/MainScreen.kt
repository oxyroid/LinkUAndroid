package com.linku.im.screen.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.bundleOf
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.extension.intervalClickable
import com.linku.im.extension.times
import com.linku.im.screen.Screen
import com.linku.im.screen.main.composable.ConversationItem
import com.linku.im.ui.components.MaterialIconButton
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.theme.LocalAnimatedColor
import com.linku.im.ui.theme.LocalNavController
import com.linku.im.ui.theme.LocalSpacing
import com.linku.im.ui.theme.LocalTheme
import com.linku.im.vm
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalPagerApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pagerState = rememberPagerState()
    val hostState = remember { SnackbarHostState() }

    val navController = LocalNavController.current
    val selections = buildList {
        Selection.Route(
            resId = R.string.notification,
            route = Screen.IntroduceScreen.withArgs(-1),
            icon = Icons.Sharp.Notifications
        ).also(::add)
        Selection.Route(
            resId = R.string.settings,
            route = Screen.IntroduceScreen.withArgs(-1),
            icon = Icons.Sharp.Settings
        ).also(::add)
        Selection.Switch(resId = R.string.toggle_theme,
            value = vm.readable.isDarkMode,
            onIcon = Icons.Sharp.LightMode,
            offIcon = Icons.Sharp.DarkMode,
            onClick = {
                vm.onEvent(LinkUEvent.ToggleDarkMode)
            }).also(::add)
    }

    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            hostState.showSnackbar(it)
        }
        vm.message.handle { hostState.showSnackbar(it) }
    }
    val theme = LocalTheme.current
    Scaffold(
        topBar = {
            val vmState = vm.readable
            ToolBar(
                navIcon = Icons.Sharp.Menu,
                onNavClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
                actions = {
                    MaterialIconButton(
                        icon = Icons.Sharp.Search,
                        onClick = {
                            navController.navigate(R.id.action_mainFragment_to_queryFragment)
                        },
                        contentDescription = "search"
                    )
                },
                text = vmState.label ?: context.getString(R.string.app_name)
            )
        },
        floatingActionButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(
                    onClick = { navController.navigate(R.id.action_mainFragment_to_createFragment) },
                    modifier = Modifier
                        .padding(LocalSpacing.current.medium)
                        .align(Alignment.End),
                    elevation = FloatingActionButtonDefaults.loweredElevation(),
                    containerColor = theme.primary,
                    contentColor = theme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Add,
                        contentDescription = null
                    )
                }
                SnackbarHost(hostState = hostState)
            }

        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = Color.Unspecified
    ) { innerPadding ->
        val pages = listOf(
            stringResource(R.string.tab_notification),
            stringResource(R.string.tab_contact),
            stringResource(R.string.tab_more)
        )
        Column(
            modifier = Modifier
                .drawWithContent {
                    drawRect(theme.background)
                    drawContent()
                }
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
                        color = theme.primary,
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
                            selectedContentColor = LocalAnimatedColor.current.onSurfaceColor,
                            unselectedContentColor = LocalAnimatedColor.current.onSurfaceColor * 0.6f,
                            modifier = Modifier
                                .padding(LocalSpacing.current.small)
                                .clip(RoundedCornerShape(LocalSpacing.current.extraSmall))
                        )
                    }
                },
                containerColor = theme.surface,
                contentColor = theme.onSurface
            )
            HorizontalPager(
                count = pages.size,
                state = pagerState
            ) { page ->
                if (page == 2) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(LocalSpacing.current.small)
                    ) {
                        items(selections) { selection ->
                            ListItem(
                                leadingContent = {
                                    Icon(
                                        imageVector = selection.icon,
                                        contentDescription = stringResource(selection.resId)
                                    )
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
                                                    else theme.error,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .padding(LocalSpacing.current.small)
                                    .clip(RoundedCornerShape(LocalSpacing.current.small))
                                    .intervalClickable {
                                        when (selection) {
                                            is Selection.Route -> {
                                                when (Screen.valueOf(selection.route)) {
                                                    Screen.IntroduceScreen -> {
                                                        navController.navigate(
                                                            if (vm.authenticator.currentUID == null)
                                                                R.id.action_mainFragment_to_signFragment
                                                            else R.id.action_mainFragment_to_introduceFragment
                                                        )
                                                    }
                                                    Screen.MainScreen -> {}
                                                    else -> navController.navigate(selection.route)
                                                }
                                            }
                                            is Selection.Switch -> selection.onClick()
                                        }
                                    },
                                colors = ListItemDefaults.colors(
                                    containerColor = theme.surface,
                                    leadingIconColor = theme.onSurface * 0.6f,
                                    trailingIconColor = theme.onSurface * 0.6f,
                                    headlineColor = theme.onSurface
                                )
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !state.loadingConversations
                    ) {
                        val conversations = when (page) {
                            0 -> state.conversations
                            1 -> state.contracts
                            else -> emptyList()
                        }
                        itemsIndexed(conversations) { index, conversation ->
                            ConversationItem(
                                conversation = conversation,
                                pinned = index < 1,
                                unreadCount = index / 2,
                                modifier = Modifier.animateItemPlacement()
                            ) {
                                navController.navigate(
                                    R.id.action_mainFragment_to_chatFragment,
                                    bundleOf(
                                        "cid" to conversation.id
                                    )
                                )
                            }
                            if (index != conversations.lastIndex) Divider()
                        }
                    }
                }
            }
        }
    }
}

private sealed class Selection(
    open val resId: Int,
    open val icon: ImageVector
) {
    data class Route(
        override val resId: Int,
        val route: String,
        override val icon: ImageVector
    ) : Selection(resId, icon)

    data class Switch(
        override val resId: Int,
        val value: Boolean,
        val onIcon: ImageVector,
        val offIcon: ImageVector = onIcon,
        val onClick: () -> Unit
    ) : Selection(resId, if (value) onIcon else offIcon)
}