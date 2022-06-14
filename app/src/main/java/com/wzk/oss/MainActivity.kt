package com.wzk.oss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wzk.oss.screen.Screen
import com.wzk.oss.screen.profile.AccountScreen
import com.wzk.oss.screen.cart.CartScreen
import com.wzk.oss.screen.detail.DetailScreen
import com.wzk.oss.screen.info.InfoScreen
import com.wzk.oss.screen.list.ListScreen
import com.wzk.oss.screen.login.LoginScreen
import com.wzk.oss.ui.theme.OssTheme
import dagger.hilt.android.AndroidEntryPoint

val activity get() = MainActivity._ins

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        lateinit var _ins: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _ins = this
        setContent {
            OssTheme {
                WindowCompat.getInsetsController(
                    window,
                    LocalView.current
                ).isAppearanceLightStatusBars = true
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                window.navigationBarColor = MaterialTheme.colorScheme.surface.toArgb()
                val navController = rememberNavController()
                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.ListScreen.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.ListScreen.route) { ListScreen(navController) }
                        composable(Screen.DetailScreen.route + "/{foodId}") {
                            DetailScreen(navController)
                        }
                        composable(Screen.LoginScreen.route) { LoginScreen(navController) }
                        composable(Screen.CartScreen.route) { CartScreen(navController) }
                        composable(Screen.InfoScreen.route) { InfoScreen(navController) }
                        composable(Screen.ProfileScreen.route) { AccountScreen(navController) }
                    }
                }
            }
        }
    }
}