package com.linku.im.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.domain.Auth
import com.linku.im.linku.LinkUEvent
import com.linku.im.screen.Screen
import com.linku.im.screen.main.composable.ConversationItem
import com.linku.im.screen.main.composable.Drawer
import com.linku.im.ui.ToolBarAction
import com.linku.im.ui.drawVerticalScrollbar
import com.linku.im.vm

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    drawerState: DrawerState
) {
    val state by mainViewModel.state
    val listState = rememberLazyListState()
    vm.onActions {
        ToolBarAction(
            onClick = { vm.onEvent(LinkUEvent.Navigate(Screen.QueryScreen)) },
            imageVector = Icons.Rounded.Search,
            tint = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary
        )
    }
    vm.onTitle {
        Text(
            text = vm.state.value.label,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
            overflow = TextOverflow.Ellipsis,
            color = if (vm.state.value.isDarkMode) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onPrimary
        )
    }
    Drawer(
        title = state.drawerTitle,
        drawerState = drawerState,
        onNavigate = { screen ->
            when (screen) {
                Screen.ProfileScreen -> {
                    val screenActual =
                        Screen.LoginScreen.takeIf { Auth.currentUID == null } ?: screen
                    vm.onEvent(LinkUEvent.Navigate(screenActual))
                }
                Screen.MainScreen -> {}
                else -> vm.onEvent(LinkUEvent.Navigate(screen))
            }
        },
        onHeaderClick = {
            mainViewModel.onEvent(MainEvent.OneWord)
        }
    ) {
        Card(
            shape = RoundedCornerShape(topStartPercent = 5, topEndPercent = 5),
            modifier = Modifier.padding(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
            )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
                            ),
                            end = Offset(0.0f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .drawVerticalScrollbar(
                        state = listState
                    ),
                userScrollEnabled = !state.loading
            ) {
                if (state.loading) {
                    repeat(12) {
                        item {
                            ConversationItem()
                            Divider()
                        }
                    }
                } else {
                    itemsIndexed(state.conversations) { index, conversation ->
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