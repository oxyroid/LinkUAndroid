package com.linku.im.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Divider
import androidx.compose.material.FabPosition
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.*
import androidx.compose.material3.*
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.extension.ifTrue
import com.linku.im.extension.intervalClickable
import com.linku.im.screen.Screen
import com.linku.im.screen.main.composable.ConversationItem
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.ToolBarAction
import com.linku.im.ui.theme.supportDynamic
import com.linku.im.vm
import kotlinx.coroutines.launch

private sealed class Selection(
    open val resId: Int,
    open val icon: ImageVector
) {
    data class Route(
        override val resId: Int,
        val screen: Screen,
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


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalPagerApi::class,
    ExperimentalMaterialApi::class, ExperimentalAnimationApi::class
)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()

    val selections = buildList {
        Selection.Route(R.string.notification, Screen.ProfileScreen, Icons.Sharp.Notifications)
            .also(::add)
        Selection.Route(R.string.settings, Screen.ProfileScreen, Icons.Sharp.Settings).also(::add)
        Selection.Switch(
            resId = R.string.toggle_theme,
            value = vm.readable.isDarkMode,
            onIcon = Icons.Sharp.LightMode,
            offIcon = Icons.Sharp.DarkMode,
            onClick = {
                vm.onEvent(LinkUEvent.ToggleDarkMode)
            }
        ).also(::add)
        supportDynamic.ifTrue {
            Selection.Switch(
                resId = R.string.toggle_dynamic,
                value = vm.readable.dynamicEnabled,
                onClick = {
                    vm.onEvent(LinkUEvent.ToggleDynamic)
                },
                onIcon = Icons.Sharp.FormatPaint
            ).also(::add)
        }
    }

    LaunchedEffect(viewModel.message, vm.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
        vm.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
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
                    ToolBarAction(
                        onClick = { vm.onEvent(LinkUEvent.Navigate(Screen.QueryScreen)) },
                        imageVector = Icons.Sharp.Search
                    )
                },
                text = vmState.label
            )
        },
        floatingActionButton = {
            AnimatedVisibility(pagerState.currentPage == 1) {
                LargeFloatingActionButton(
                    onClick = { /*TODO*/ }
                ) {
                    Icon(
                        imageVector = Icons.Sharp.Add,
                        contentDescription = ""
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        val pages = listOf(
            stringResource(R.string.tab_notification),
            stringResource(R.string.tab_contact),
            stringResource(R.string.tab_more)
        )

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            TabRow(
                modifier = Modifier.wrapContentWidth(),
                selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(
                                tabPositions[pagerState.currentPage]
                            )
                            .padding(horizontal = 24.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp
                                )
                            ),
                        color = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onPrimary,
                        height = 5.dp
                    )
                },
                divider = {},
                tabs = {
                    pages.forEachIndexed { index, page ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = page,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            selectedContentColor = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onPrimary,
                            unselectedContentColor = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                },
                containerColor = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                contentColor = if (vm.readable.isDarkMode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
            )
            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.background(MaterialTheme.colorScheme.background)
            ) { page ->
                if (page == 2) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(selections) { selection ->
                                ListItem(
                                    icon = {
                                        Icon(
                                            imageVector = selection.icon,
                                            contentDescription = stringResource(selection.resId)
                                        )
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(selection.resId),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = LocalContentColor.current
                                        )
                                    },
                                    trailing = {
                                        if (selection is Selection.Switch) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        color = if (selection.value) Color.Green
                                                        else MaterialTheme.colorScheme.error,
                                                        shape = RoundedCornerShape(50)
                                                    )
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .intervalClickable {
                                            when (selection) {
                                                is Selection.Route -> {
                                                    when (val screen = selection.screen) {
                                                        Screen.ProfileScreen -> {
                                                            val screenActual =
                                                                Screen.LoginScreen
                                                                    .takeIf { vm.authenticator.currentUID == null }
                                                                    ?: screen
                                                            vm.onEvent(
                                                                LinkUEvent.Navigate(
                                                                    screenActual
                                                                )
                                                            )
                                                        }
                                                        Screen.MainScreen -> {}
                                                        else -> vm.onEvent(
                                                            LinkUEvent.Navigate(
                                                                screen
                                                            )
                                                        )
                                                    }
                                                }
                                                is Selection.Switch -> {
                                                    selection.onClick()
                                                }
                                            }
                                        }
                                )
                            }

                        }

                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = !state.loading
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
                                vm.onEvent(
                                    LinkUEvent.NavigateWithArgs(
                                        Screen.ChatScreen.withArgs(conversation.id)
                                    )
                                )
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }
}