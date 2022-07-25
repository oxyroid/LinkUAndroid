package com.linku.im

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.im.extension.ifTrue
import com.linku.im.linku.LinkUEvent
import com.linku.im.linku.LinkUViewModel
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.introduce.ProfileScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.query.QueryScreen
import com.linku.im.screen.sign.LoginScreen
import com.linku.im.ui.ToolBar
import com.linku.im.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val _vm: LinkUViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = _vm
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { App() }
    }
}

lateinit var vm: LinkUViewModel
    private set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val state by vm.state
    @OptIn(ExperimentalAnimationApi::class)
    AppTheme(
        useDarkTheme = state.isDarkMode,
        enableDynamic = state.dynamicEnabled
    ) {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = isSystemInDarkTheme()

        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons
            )
        }

        val coroutineScope = rememberCoroutineScope()
        val navController = rememberAnimatedNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        LaunchedEffect(Unit) {
            vm.onEvent(LinkUEvent.InitNavController(navController))
            vm.onEvent(LinkUEvent.InitScaffoldState(coroutineScope, drawerState))
        }

        ProvideWindowInsets {
            Scaffold(
                topBar = {
                    ToolBar(
                        navIcon = (state.currentScreen == Screen.MainScreen && drawerState.isOpen)
                            .ifTrue { Icons.Rounded.Close }
                            ?: state.icon,
                        title = state.title,
                        onNavClick = state.navClick,
                        actions = state.actions,
                        isDarkMode = state.isDarkMode
                    )
                }
            ) { innerPadding ->
                val specFloat = tween<Float>(400)
                AnimatedNavHost(
                    navController = navController,
                    startDestination = Screen.MainScreen.route,
                    modifier = Modifier.padding(innerPadding),
                    enterTransition = { fadeIn(animationSpec = specFloat) },
                    exitTransition = { fadeOut(animationSpec = specFloat) }
                ) {
                    composable(route = Screen.MainScreen.route) {
                        MainScreen(drawerState = drawerState)
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
                    composable(Screen.ProfileScreen.route) { ProfileScreen() }
                    composable(Screen.QueryScreen.route) { QueryScreen() }
                }
            }
        }
    }
}