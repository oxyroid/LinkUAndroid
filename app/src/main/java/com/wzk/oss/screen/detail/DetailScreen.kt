package com.wzk.oss.screen.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.wzk.oss.ui.MaterialButton
import com.wzk.oss.ui.MaterialSnackHost
import com.wzk.oss.ui.shimmerBrush
import com.wzk.domain.LocalSharedPreference
import com.wzk.oss.R
import com.wzk.oss.application
import com.wzk.oss.screen.Screen
import com.wzk.oss.screen.detail.composable.DetailBottomIconButton
import com.wzk.oss.screen.detail.composable.DetailIntroduce
import com.wzk.oss.screen.detail.composable.DetailLabel
import com.wzk.oss.screen.detail.composable.DetailScreenBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    navController: NavController, viewModel: DetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    /**
     * If the local account is legal, running this code-blocking.
     * Otherwise navigate to the LoginScreen.
     */
    fun legalAccount(block: () -> Unit) {
        if (LocalSharedPreference.getLocalUserId() == 0) {
            scope.launch {
                val result = scaffoldState.snackbarHostState.showSnackbar(
                    application.getString(R.string.warn_account_required),
                    application.getString(R.string.warn_account_required_action)
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> navController.navigate(Screen.LoginScreen.route)
                    SnackbarResult.Dismissed -> {}
                }
            }
        } else block()
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = { DetailScreenBar(navController::popBackStack) },
        snackbarHost = {
            MaterialSnackHost(state = scaffoldState.snackbarHostState)
        },
        bottomBar = {
            BottomAppBar(icons = {
                Spacer(Modifier.width(4.dp))
                DetailBottomIconButton(
                    imageVector = Icons.Default.ShoppingCart, text = stringResource(R.string.cart)
                ) {
                    legalAccount {
                        navController.navigate(Screen.CartScreen.route)
                    }
                }
            }, floatingActionButton = {
                MaterialButton(
                    textRes = R.string.add_to_cart,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    enabled = !state.adding && !state.loading
                ) {
                    state.food?.also { food ->
                        legalAccount {
                            viewModel.onEvent(DetailEvent.AddToCart(food.id))
                        }
                    }
                }
            })
        },
        backgroundColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val lazyListState = rememberLazyListState()
        LazyColumn(
            state = lazyListState, modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val food = state.food
            item {
                if (food == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4 / 3f)
                            .background(brush = shimmerBrush()),
                    )
                } else {
                    SubcomposeAsyncImage(
                        model = food.img,
                        contentDescription = food.description,
                        modifier = Modifier.fillMaxWidth(),
                        loading = {
                            CircularProgressIndicator()
                        },
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
            stickyHeader {
                DetailLabel(food?.name)
            }

            item {
                DetailIntroduce(
                    introduce = food?.description, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    LaunchedEffect(state) {
        state.addEvent.handle {
            scaffoldState.snackbarHostState.showSnackbar(application.getString(R.string.add_to_cart_success))
        }
        state.error.handle {
            scaffoldState.snackbarHostState.showSnackbar(it.first)
        }
    }
}