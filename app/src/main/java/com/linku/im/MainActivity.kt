package com.linku.im

import android.os.Bundle
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.linku.im.global.LinkUEvent
import com.linku.im.global.LinkUViewModel
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.info.InfoScreen
import com.linku.im.screen.introduce.ProfileScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.sign.LoginScreen
import com.linku.im.ui.MaterialSnackHost
import com.linku.im.ui.ToolBar
import com.linku.im.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val _vm: LinkUViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = _vm
        setContent { App(window = window) }
    }
}

lateinit var vm: LinkUViewModel
    private set

@Composable
fun App(
    window: Window
) {
    val state by vm.state
    @OptIn(ExperimentalAnimationApi::class)
    AppTheme(state.isDarkMode) {
        val color = if (!state.isDarkMode) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface
        val contentColor = if (!state.isDarkMode) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.surface
        WindowCompat.getInsetsController(window, LocalView.current)
            .isAppearanceLightStatusBars = false

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = color.toArgb()
        window.navigationBarColor = contentColor.toArgb()

        val coroutineScope = rememberCoroutineScope()
        val navController = rememberAnimatedNavController()
        val scaffoldState = rememberScaffoldState()

        LaunchedEffect(Unit) {
            vm.onEvent(LinkUEvent.InitNavController(navController))
            vm.onEvent(LinkUEvent.InitScaffoldState(coroutineScope, scaffoldState))
        }

        Scaffold(
            topBar = {
                ToolBar(
                    navIcon = state.icon,
                    title = state.title,
                    onNavClick = state.navClick,
                    isDarkMode = state.isDarkMode,
                    actions = state.actions
                )
            },
            snackbarHost = { MaterialSnackHost(scaffoldState.snackbarHostState) },
            scaffoldState = scaffoldState,
            drawerBackgroundColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            val specIntOffset = tween<IntOffset>(400)
            val specFloat = tween<Float>(400)
            AnimatedNavHost(
                navController = navController,
                startDestination = Screen.MainScreen.route,
                modifier = Modifier
                    .padding(innerPadding)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                color,
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surface
                            ),
                            end = Offset(0.0f, Float.POSITIVE_INFINITY)
                        )
                    )
                    .navigationBarsPadding(),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentScope.SlideDirection.Up,
                        animationSpec = specIntOffset
                    )
                },
                exitTransition = {
                    slideOut(
                        animationSpec = specIntOffset,
                        targetOffset = { IntOffset(0, (it.height * -0.3).toInt()) }
                    ) + fadeOut(
                        animationSpec = specFloat
                    )
                },
                popEnterTransition = {
                    slideIn(
                        animationSpec = specIntOffset,
                        initialOffset = { IntOffset(0, (it.height * -0.3).toInt()) }
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentScope.SlideDirection.Down,
                        animationSpec = specIntOffset
                    ) + fadeOut(
                        animationSpec = specFloat
                    )
                }) {
                composable(
                    route = Screen.MainScreen.route,
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentScope.SlideDirection.Up,
                            animationSpec = specIntOffset
                        )
                    }
                ) {
                    MainScreen(scaffoldState = scaffoldState)
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
                composable(Screen.ProfileScreen.route) { ProfileScreen() }
            }
        }
    }
}