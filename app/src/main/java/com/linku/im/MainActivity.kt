package com.linku.im

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.im.extension.ifTrue
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.edit.EditScreen
import com.linku.im.screen.introduce.IntroduceEvent
import com.linku.im.screen.introduce.IntroduceScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.query.QueryScreen
import com.linku.im.screen.sign.LoginScreen
import com.linku.im.ui.theme.AppTheme
import com.linku.im.ui.theme.LocalNavController
import com.linku.im.ui.theme.LocalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity(), ViewTreeObserver.OnPreDrawListener {
    private val _vm: LinkUViewModel by viewModels()
    private lateinit var content: View

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        vm = _vm
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent { App() }

        content = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(this)

    }

    override fun onPreDraw(): Boolean {
        return vm.readable.isReady.ifTrue {
            content.viewTreeObserver.removeOnPreDrawListener(this)
            true
        } ?: false
    }

    override fun onDestroy() {
        super.onDestroy()
        _vm.onEvent(LinkUEvent.Disconnect)
    }
}

lateinit var vm: LinkUViewModel
    private set

@Composable
fun App() {
    val state = vm.readable
    val isDarkMode = state.isDarkMode
    @OptIn(ExperimentalAnimationApi::class)
    AppTheme(
        useDarkTheme = isDarkMode
    ) {
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(isDarkMode) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = !isDarkMode
            )
            systemUiController.setNavigationBarColor(
                color = Color.Transparent,
                darkIcons = !isDarkMode
            )
        }

        AnimatedNavHost(
            navController = LocalNavController.current,
            startDestination = Screen.MainScreen.route,
            modifier = Modifier
                .background(LocalTheme.current.background),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(400),
                    transformOrigin = TransformOrigin(0f, 0.5f)
                )
            },
            popEnterTransition = { fadeIn() },
            popExitTransition = { fadeOut() }
        ) {
            composable(
                route = Screen.MainScreen.route
            ) {
                MainScreen()
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
            composable(
                route = Screen.IntroduceScreen.buildArgs("uid"),
                arguments = listOf(
                    navArgument("uid") {
                        type = NavType.IntType
                        nullable = false
                    }
                )
            ) { entity ->
                IntroduceScreen(entity.arguments?.getInt("uid") ?: -1)
            }
            composable(Screen.QueryScreen.route) { QueryScreen() }
            composable(
                route = Screen.EditScreen.buildArgs("type"),
                arguments = listOf(
                    navArgument("type") {
                        type = NavType.IntType
                        nullable = false
                    }
                )
            ) { entity ->
                EditScreen(
                    type = entity.arguments
                        ?.getInt("type")
                        ?.let(IntroduceEvent.Edit.Type::parse)
                )
            }
        }
    }
}