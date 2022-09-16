package com.linku.im

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.linku.im.extension.ifTrue
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ViewTreeObserver.OnPreDrawListener {
    private val _vm: LinkUViewModel by viewModels()
    private lateinit var content: View

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        vm = _vm
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)
        content = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(this)
    }

    override fun onPreDraw(): Boolean {
        return vm.readable.isEmojiReady.ifTrue {
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

//@Composable
//fun App() {
//    val state = vm.readable
//    val isDarkMode = state.isDarkMode
//    @OptIn(ExperimentalAnimationApi::class)
//    AppTheme(
//        useDarkTheme = isDarkMode
//    ) {
//        val systemUiController = rememberSystemUiController()
//        LaunchedEffect(isDarkMode) {
//            systemUiController.setSystemBarsColor(
//                color = Color.Transparent,
//                darkIcons = !isDarkMode
//            )
//        }
//        val floatTweenSpec = remember { tween<Float>(200) }
//        val intOffsetTweenSpec = remember { tween<IntOffset>(200) }
//        AnimatedNavHost(
//            navController = LocalNavController.current,
//            startDestination = Screen.MainScreen.route,
//            enterTransition = {
//                slideInHorizontally(
//                    initialOffsetX = { it / 2 },
//                    animationSpec = intOffsetTweenSpec
//                ) + fadeIn(
//                    animationSpec = floatTweenSpec,
//                    initialAlpha = 0.5f
//                )
//            },
//            exitTransition = {
//                fadeOut(
//                    animationSpec = floatTweenSpec,
//                    targetAlpha = 1f
//                )
//            },
//            popEnterTransition = {
//                fadeIn(
//                    animationSpec = floatTweenSpec,
//                    initialAlpha = 1f
//                )
//            },
//            popExitTransition = {
//                slideOutHorizontally(
//                    targetOffsetX = { it },
//                    animationSpec = intOffsetTweenSpec
//                ) + fadeOut(
//                    animationSpec = floatTweenSpec,
//                    targetAlpha = 0.5f
//                )
//            }
//        ) {
//            composable(
//                route = Screen.MainScreen.route
//            ) {
//                MainScreen()
//            }
//            composable(
//                route = Screen.ChatScreen.buildArgs("cid"),
//                arguments = listOf(
//                    navArgument("cid") {
//                        type = NavType.IntType
//                        nullable = false
//                    }
//                )
//            ) { entry ->
//                ChatScreen(
//                    cid = entry.arguments?.getInt("cid") ?: -1
//                )
//            }
//            composable(Screen.LoginScreen.route) { LoginScreen() }
//            composable(
//                route = Screen.IntroduceScreen.buildArgs("uid"),
//                arguments = listOf(
//                    navArgument("uid") {
//                        type = NavType.IntType
//                        nullable = false
//                    }
//                )
//            ) { entity ->
//                IntroduceScreen(entity.arguments?.getInt("uid") ?: -1)
//            }
//            composable(Screen.QueryScreen.route) { QueryScreen() }
//            composable(
//                route = Screen.EditScreen.buildArgs("type"),
//                arguments = listOf(
//                    navArgument("type") {
//                        type = NavType.IntType
//                        nullable = false
//                    }
//                )
//            ) { entity ->
//                EditScreen(
//                    type = entity.arguments
//                        ?.getInt("type")
//                        ?.let(IntroduceEvent.Edit.Type::parse)
//                )
//            }
//
//            composable(Screen.CreateScreen.route) { CreateScreen() }
//        }
//    }
//}