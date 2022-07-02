package com.linku.im

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.linku.im.extension.toggle
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.info.InfoScreen
import com.linku.im.screen.login.LoginScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.profile.AccountScreen
import com.linku.im.ui.MaterialSnackHost
import com.linku.im.ui.ToolBar
import com.linku.im.ui.theme.OssTheme
import com.tencent.mmkv.MMKV
import dagger.hilt.android.AndroidEntryPoint

val activity get() = MainActivity.lazyActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val navViewModel: NavViewModel by viewModels()

    companion object {
        lateinit var lazyActivity: MainActivity
    }

    override fun onPause() {
        MMKV.defaultMMKV().encode(NavViewModel.SAVED_DARK_MODE, navViewModel.isDarkMode.value)
        super.onPause()
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyActivity = this
        setContent {
            val isDarkMode by navViewModel.isDarkMode
            OssTheme(isDarkMode) {
                WindowCompat.getInsetsController(
                    window, LocalView.current
                ).isAppearanceLightStatusBars = !isDarkMode

                WindowCompat.setDecorFitsSystemWindows(window, false)
                window.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
                window.navigationBarColor = MaterialTheme.colorScheme.background.toArgb()

                val navController = rememberAnimatedNavController()
                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                Scaffold(
                    topBar = {
                        ToolBar(
                            navIcon = navViewModel.rememberedIcon.value,
                            title = navViewModel.rememberedTitle.value,
                            actions = navViewModel.rememberedActions.value,
                            onNavClick = navViewModel.rememberedOnNavClick.value
                        )
                    },
                    snackbarHost = {
                        MaterialSnackHost(scaffoldState.snackbarHostState)
                    },
                    scaffoldState = scaffoldState,
                    drawerBackgroundColor = MaterialTheme.colorScheme.background,
                    backgroundColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Column {
                        AnimatedNavHost(
                            navController = navController,
                            startDestination = Screen.MainScreen.route,
                            modifier = Modifier
                                .padding(innerPadding)
                                .background(MaterialTheme.colorScheme.background)
                                .navigationBarsPadding(),
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentScope.SlideDirection.Start,
                                    animationSpec = tween(
                                        durationMillis = 400
                                    )
                                )
                            },
                            exitTransition = {
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = 400
                                    )
                                )
                            },
                            popEnterTransition = {
                                fadeIn(
                                    animationSpec = tween(
                                        durationMillis = 400
                                    )
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentScope.SlideDirection.End,
                                    animationSpec = tween(
                                        durationMillis = 400
                                    )
                                )
                            }
                        ) {
                            composable(Screen.MainScreen.route) {
                                MainScreen(
                                    navController = navController,
                                    scaffoldState = scaffoldState,
                                    navViewModel = navViewModel
                                ) {
                                    scaffoldState.drawerState.toggle(scope)
                                }
                            }
                            composable(Screen.ChatScreen.route + "/{cid}") {
                                ChatScreen(
                                    navController = navController,
                                    navViewModel = navViewModel
                                )
                            }
                            composable(Screen.LoginScreen.route) {
                                LoginScreen(
                                    navController,
                                    navViewModel = navViewModel
                                )
                            }
                            composable(Screen.InfoScreen.route) {
                                InfoScreen(
                                    navController,
                                    navViewModel = navViewModel
                                )
                            }
                            composable(Screen.ProfileScreen.route) {
                                AccountScreen(
                                    navController,
                                    navViewModel = navViewModel
                                )
                            }
                        }
                    }

                }
            }
        }
    }

}