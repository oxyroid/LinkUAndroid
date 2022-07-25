package com.linku.im.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        )
    }
    vm.onTitle {
        Text(
            text = vm.state.value.label,
            style = MaterialTheme.typography.titleMedium
        )
    }
    Drawer(
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
        }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
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