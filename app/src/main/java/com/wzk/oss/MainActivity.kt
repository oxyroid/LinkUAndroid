package com.wzk.oss

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tencent.mmkv.MMKV
import com.wzk.oss.screen.Screen
import com.wzk.oss.screen.cart.CartScreen
import com.wzk.oss.screen.detail.DetailScreen
import com.wzk.oss.screen.info.InfoScreen
import com.wzk.oss.screen.list.ListScreen
import com.wzk.oss.screen.login.LoginScreen
import com.wzk.oss.screen.profile.AccountScreen
import com.wzk.oss.ui.theme.OssTheme
import dagger.hilt.android.AndroidEntryPoint

val activity get() = MainActivity.lazyActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var savedDarkMode = false

    companion object {
        lateinit var lazyActivity: MainActivity
        private const val SAVED_DARK_MODE = "saved:dark-mode"
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(SAVED_DARK_MODE, savedDarkMode)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        MMKV.defaultMMKV().putBoolean(SAVED_DARK_MODE, savedDarkMode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyActivity = this
        savedDarkMode = savedInstanceState
            ?.getBoolean(SAVED_DARK_MODE)
            ?: MMKV.defaultMMKV()
                .getBoolean(SAVED_DARK_MODE, false)
        setContent {
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }

            fun toggleTheme() {
                isDarkMode = !isDarkMode
                savedDarkMode = isDarkMode
            }

            OssTheme(isDarkMode) {
                WindowCompat.getInsetsController(window, LocalView.current)
                    .isAppearanceLightStatusBars = !isDarkMode

                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                window.navigationBarColor = MaterialTheme.colorScheme.surface.toArgb()

                val navController = rememberNavController()
                Scaffold { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.ListScreen.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.ListScreen.route) {
                            ListScreen(navController, toggleTheme = ::toggleTheme)
                        }
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