package com.wzk.oss.screen.cart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.wzk.oss.ui.MaterialTopBar
import com.wzk.oss.R
import com.wzk.oss.screen.cart.composable.CartItem

@Composable
fun CartScreen(
    navController: NavController,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state
    Scaffold(
        topBar = {
            MaterialTopBar(
                title = stringResource(R.string.cart),
                onNavClick = navController::popBackStack
            )
        }
    ) {
        LazyColumn {
            items(state.foods) { food ->
                CartItem(
                    food = food,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}