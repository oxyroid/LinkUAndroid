package com.linku.im

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.info.InfoScreen
import com.linku.im.screen.login.LoginScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.overall.OverallEvent
import com.linku.im.screen.overall.OverallViewModel
import com.linku.im.screen.profile.AccountScreen
import com.linku.im.ui.MaterialSnackHost
import com.linku.im.ui.ToolBar
import com.linku.im.ui.theme.OssTheme
import dagger.hilt.android.AndroidEntryPoint

val outsideContent get() = MainActivity.outsideContent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: OverallViewModel by viewModels()

    companion object {
        lateinit var outsideContent: MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        outsideContent = this

        overall = vm
        setContent { App(window = window) }
    }
}

lateinit var overall: OverallViewModel
    private set

@Composable
fun App(
    window: Window
) {
    val state by overall.state
    @OptIn(ExperimentalAnimationApi::class)
    OssTheme(state.isDarkMode) {
        WindowCompat.getInsetsController(window, LocalView.current)
            .isAppearanceLightStatusBars = !state.isDarkMode

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = MaterialTheme.colorScheme.background.toArgb()
        window.navigationBarColor = MaterialTheme.colorScheme.background.toArgb()

        val coroutineScope = rememberCoroutineScope()
        val navController = rememberAnimatedNavController()
        val scaffoldState = rememberScaffoldState()
        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            overall.onEvent(OverallEvent.InitNavController(navController))
            overall.onEvent(OverallEvent.InitScaffoldState(coroutineScope, scaffoldState))
        }

        Scaffold(
            topBar = {
                ToolBar(
                    navIcon = state.icon,
                    title = state.title,
                    onNavClick = state.navClick,
                    onScroll = listState.isScrollInProgress,
                    onMenuClick = { overall.onEvent(OverallEvent.ToggleTheme) }
                )
            },
            snackbarHost = { MaterialSnackHost(scaffoldState.snackbarHostState) },
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
                            animationSpec = tween(400)
                        )
                    },
                    exitTransition = {
                        slideOut(
                            animationSpec = tween(400),
                            targetOffset = { IntOffset((it.width * -0.3).toInt(), 0) }
                        )
                    },
                    popEnterTransition = {
                        slideIn(
                            animationSpec = tween(400),
                            initialOffset = { IntOffset((it.width * -0.3).toInt(), 0) }
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentScope.SlideDirection.End,
                            animationSpec = tween(400)
                        )
                    }) {
                    composable(Screen.MainScreen.route) {
                        MainScreen(scaffoldState = scaffoldState, listState = listState)
                    }
                    composable(
                        route = Screen.ChatScreen.buildArgs("cid"),
                        arguments = listOf(
                            navArgument("cid") {
                                type = NavType.IntType
                                nullable = false
                            }
                        )
                    ) { entry ->
                        ChatScreen(
                            cid = entry.arguments?.getInt("cid") ?: -1
                        )
                    }
                    composable(Screen.LoginScreen.route) { LoginScreen() }
                    composable(Screen.InfoScreen.route) { InfoScreen() }
                    composable(Screen.ProfileScreen.route) { AccountScreen() }
                }
            }
        }
    }
}