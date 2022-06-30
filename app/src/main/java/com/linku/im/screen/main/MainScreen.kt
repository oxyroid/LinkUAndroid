package com.linku.im.screen.main

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.FabPosition
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.linku.im.extension.toggle
import com.linku.im.screen.Screen
import com.linku.im.screen.main.composable.MainConversationItem
import com.linku.im.screen.main.composable.MainDrawer
import com.linku.im.ui.MaterialSnackHost
import com.linku.im.ui.MaterialTopBar
import com.linku.im.ui.verticalScrollbar

@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel = hiltViewModel(),
    toggleTheme: () -> Unit
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val state by mainViewModel.state
    Scaffold(
        topBar = {
            MaterialTopBar(
                navIcon = Icons.Default.Menu,
                title = state.title
            ) {
                scaffoldState.drawerState.toggle(scope)
            }
        },
        snackbarHost = {
            MaterialSnackHost(scaffoldState.snackbarHostState)
        },
        drawerBackgroundColor = MaterialTheme.colorScheme.background,
        floatingActionButtonPosition = FabPosition.End,
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colorScheme.background
    ) {
        MainDrawer(
            drawerState = scaffoldState.drawerState,
            onNavigate = {
                navController.navigate(it.route)
            }
        ) {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.verticalScrollbar(
                    state = lazyListState,
                    color = MaterialTheme.colorScheme.tertiary
                )
            ) {
                itemsIndexed(state.conversations) { index, conversation ->
                    MainConversationItem(conversation) {
                        navController.navigate(Screen.ChatScreen.route + "/0/0")
                    }
                    if (index != state.conversations.size - 1) Divider()
                }
            }
        }

    }
}