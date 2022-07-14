package com.linku.im.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.ScaffoldState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.linku.domain.Auth
import com.linku.im.overall
import com.linku.im.screen.Screen
import com.linku.im.screen.main.composable.MainConversationItem
import com.linku.im.screen.main.composable.MainDrawer
import com.linku.im.screen.overall.OverallEvent
import com.linku.im.ui.drawVerticalScrollbar

@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState,
    listState: LazyListState
) {
    val state by mainViewModel.state
    MainDrawer(
        title = state.drawerTitle,
        drawerState = scaffoldState.drawerState,
        onNavigate = { screen ->
            when (screen) {
                Screen.ProfileScreen -> {
                    val screenActual =
                        Screen.LoginScreen.takeIf { Auth.currentUID == null } ?: screen
                    overall.onEvent(OverallEvent.Navigate(screenActual))
                }

                else -> overall.onEvent(OverallEvent.Navigate(screen))
            }
        },
        onHeaderClick = {
            mainViewModel.onEvent(MainEvent.OneWord)
        }
    ) {
        FloatingActionButton(
            onClick = { /*TODO*/ },
        ) {

        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .drawVerticalScrollbar(
                    state = listState
                ),
            userScrollEnabled = !state.loading
        ) {
            if (state.loading) {
                repeat(12) {
                    item {
                        MainConversationItem()
                        Divider()
                    }
                }
            } else {
                itemsIndexed(state.conversations) { index, conversation ->
                    MainConversationItem(
                        conversation,
                        pinned = index < 1,
                        unreadCount = index / 2
                    ) {
                        overall.onEvent(
                            OverallEvent.NavigateWithArgs(
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