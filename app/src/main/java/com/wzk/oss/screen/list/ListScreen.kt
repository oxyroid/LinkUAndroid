package com.wzk.oss.screen.list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wzk.oss.ui.MaterialSnackHost
import com.wzk.domain.LocalSharedPreference
import com.wzk.oss.R
import com.wzk.oss.application
import com.wzk.oss.screen.OrderType
import com.wzk.oss.screen.Screen
import com.wzk.oss.screen.list.composable.FoodShimmerItem
import com.wzk.oss.screen.list.composable.ListDrawer
import com.wzk.oss.screen.list.composable.ListScreenBar
import kotlinx.coroutines.launch

private val menuItems = listOf(
    ListEvent.Order(ListOrder.Name()) + R.string.sorted_name,
    ListEvent.Order(ListOrder.Name(OrderType.Descending)) + R.string.sorted_name_des,
    ListEvent.Order(ListOrder.Price()) + R.string.sorted_price,
    ListEvent.Order(ListOrder.Price(OrderType.Descending)) + R.string.sorted_price_des
)

@Composable
fun ListScreen(
    navController: NavController,
    viewModel: ListViewModel = hiltViewModel(),
) {
    val state by viewModel.state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scaffoldState = rememberScaffoldState(drawerState)
    val scope = rememberCoroutineScope()
    var selectedMenuItem by remember { mutableStateOf(0) }

    /**
     * If the local account is legal, running this code-blocking.
     * Otherwise navigate to the LoginScreen.
     */
    fun legalAccount(block: () -> Unit) {
        if (LocalSharedPreference.getLocalUserId() == 0) {
            scope.launch {
                when (
                    scaffoldState.snackbarHostState.showSnackbar(
                        application.getString(R.string.warn_account_required),
                        application.getString(R.string.warn_account_required_action)
                    )
                ) {
                    SnackbarResult.ActionPerformed -> navController.navigate(Screen.LoginScreen.route)
                    SnackbarResult.Dismissed -> {}
                }
            }

        } else block()

    }

    Scaffold(
        topBar = {
            ListScreenBar(
                onNavClick = {
                    scope.launch {
                        if (scaffoldState.drawerState.isOpen) {
                            scaffoldState.drawerState.close()
                        } else scaffoldState.drawerState.open()
                    }
                },
                onItemClick = {
                    selectedMenuItem = it
                    viewModel.onEvent(menuItems[it].event)
                },
                items = menuItems,
                selectedIndex = selectedMenuItem
            )
        },
        snackbarHost = {
            MaterialSnackHost(state = scaffoldState.snackbarHostState)
        },
        drawerBackgroundColor = MaterialTheme.colorScheme.background,
        floatingActionButtonPosition = FabPosition.End,
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val lazyGridState = rememberLazyGridState()
        ListDrawer(
            drawerState = scaffoldState.drawerState,
            onNavigate = { screen ->
                when (screen) {
                    Screen.ProfileScreen, Screen.CartScreen -> {
                        legalAccount {
                            navController.navigate(screen.route)
                        }
                    }
                    Screen.InfoScreen -> {
                        navController.navigate(screen.route)
                    }
                    else -> {}
                }

            }
        ) {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .animateContentSize()
            ) {
                items(state.list) { food ->
                    FoodShimmerItem(food) { navController.navigate(Screen.DetailScreen.route + "/$it") }
                }
            }
        }
    }

    LaunchedEffect(state) {
        state.error.handle {
            scaffoldState.snackbarHostState.showSnackbar(it.first)
        }
    }
}