package com.wzk.oss.screen.list.composable

import androidx.compose.foundation.background
import androidx.compose.material.DrawerState
import androidx.compose.material.ModalDrawer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.wzk.oss.R
import com.wzk.oss.screen.Screen
import com.wzk.oss.screen.list.drawer.ListDrawerBody
import com.wzk.oss.screen.list.drawer.ListDrawerHeader
import com.wzk.oss.screen.list.drawer.ListDrawerItemDTO
import kotlinx.coroutines.launch


private val listDrawerItems = listOf(
    ListDrawerItemDTO(R.string.account, Screen.ProfileScreen, Icons.Rounded.AccountCircle),
    ListDrawerItemDTO(R.string.cart, Screen.CartScreen, Icons.Rounded.ShoppingCart),
    ListDrawerItemDTO(R.string.info, Screen.InfoScreen, Icons.Rounded.Info)
)

@Composable
fun ListDrawer(
    drawerState: DrawerState,
    onNavigate: (Screen) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            ListDrawerHeader(modifier = Modifier.background(MaterialTheme.colorScheme.background))
            ListDrawerBody(
                items = listDrawerItems,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .weight(1f)
            ) {
                scope.launch { drawerState.close() }
                onNavigate(it.screen)
            }
        },
        content = content
    )
}