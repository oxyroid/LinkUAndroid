package com.linku.im.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.linku.im.LinkUEvent
import com.linku.im.R
import com.linku.im.screen.Screen
import com.linku.im.screen.main.composable.ConversationItem
import com.linku.im.screen.main.composable.Drawer
import com.linku.im.ui.components.ToolBar
import com.linku.im.ui.components.ToolBarAction
import com.linku.im.vm
import kotlinx.coroutines.launch

internal data class PageData(
    val title: String,
    val painter: Painter
)

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class
)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state = viewModel.readable
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    LaunchedEffect(viewModel.message) {
        viewModel.message.handle {
            scaffoldState.snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            val vmState = vm.readable
            ToolBar(
                navIcon = Icons.Sharp.Close
                    .takeIf { drawerState.isOpen }
                    ?: Icons.Sharp.Menu,
                onNavClick = {
                    if (drawerState.isOpen) {
                        scope.launch { drawerState.close() }
                    } else {
                        scope.launch { drawerState.open() }
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
        }
    ) { innerPadding ->
        Drawer(
            drawerState = drawerState,
            onNavigate = { screen ->
                when (screen) {
                    Screen.ProfileScreen -> {
                        val screenActual =
                            Screen.LoginScreen
                                .takeIf { vm.authenticator.currentUID == null }
                                ?: screen
                        vm.onEvent(LinkUEvent.Navigate(screenActual))
                    }
                    Screen.MainScreen -> {}
                    else -> vm.onEvent(LinkUEvent.Navigate(screen))
                }
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            val pagerState = rememberPagerState()

            val pages = listOf(
                PageData(
                    stringResource(R.string.tab_notification),
                    painterResource(R.drawable.tab_notification)
                ),
                PageData(
                    stringResource(R.string.tab_contact),
                    painterResource(R.drawable.tab_contact)
                )
            )

            Column {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage])
                        )
                    },
                    divider = {},
                    tabs = {
                        pages.forEachIndexed { index, page ->
                            LeadingIconTab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = page.title,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                },
                                icon = {
                                    Icon(
                                        painter = page.painter,
                                        contentDescription = page.title,
                                        tint = Color.Unspecified
                                    )
                                },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
                HorizontalPager(
                    count = pages.size,
                    state = pagerState,
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) { page ->
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