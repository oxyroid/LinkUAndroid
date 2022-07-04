package com.linku.im

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.linku.domain.Auth
import com.linku.im.extension.toggle
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.global.GlobalViewModel
import com.linku.im.screen.info.InfoScreen
import com.linku.im.screen.login.LoginScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.profile.AccountScreen
import com.linku.im.ui.MaterialSnackHost
import com.linku.im.ui.ToolBar
import com.linku.im.ui.theme.OssTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

val activity get() = MainActivity.lazyActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        lateinit var lazyActivity: MainActivity
    }

    override fun onPause() {
        // MMKV.defaultMMKV().encode(GlobalViewModel.SAVED_DARK_MODE, vm.isDarkMode.value)
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lazyActivity = this

        setContent {
            App(window = window)
        }
    }

}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun App(
    window: Window,
    vm: GlobalViewModel = hiltViewModel()
) {
    val isDarkMode by vm.isDarkMode
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

        LaunchedEffect(true, block = {
            Auth.observeCurrent.onEach { user ->
                if (user == null) {
                    vm.disconnect()
                } else {
                    vm.connectToChat()
                }
            }.launchIn(this)
        })

        Scaffold(
            topBar = {
                ToolBar(
                    navIcon = vm.icon.value,
                    title = vm.title.value,
                    actions = vm.actions.value,
                    onNavClick = vm.navClick.value
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
                AnimatedNavHost(navController = navController,
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
                    }) {
                    composable(Screen.MainScreen.route) {
                        MainScreen(
                            navController = navController,
                            scaffoldState = scaffoldState,
                            globalViewModel = vm
                        ) {
                            scaffoldState.drawerState.toggle(scope)
                        }
                    }
                    composable(
                        route = Screen.ChatScreen.args("cid"),
                        arguments = listOf(navArgument("cid") {
                            type = NavType.IntType
                            nullable = false
                        })
                    ) { entry ->
                        ChatScreen(
                            navController = navController,
                            vm = vm,
//                            cid = entry.arguments?.getInt("cid")
                            // FIXME
                            cid = 1,
                        )
                    }
                    composable(Screen.LoginScreen.route) {
                        LoginScreen(
                            navController = navController, globalViewModel = vm
                        )
                    }
                    composable(Screen.InfoScreen.route) {
                        InfoScreen(
                            navController = navController, globalViewModel = vm
                        )
                    }
                    composable(Screen.ProfileScreen.route) {
                        AccountScreen(
                            navController = navController, globalViewModel = vm
                        )
                    }
                }
            }

        }
    }
}