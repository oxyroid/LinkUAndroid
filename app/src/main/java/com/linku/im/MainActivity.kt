package com.linku.im

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.domain.Authenticator
import com.linku.im.extension.ifTrue
import com.linku.im.screen.Screen
import com.linku.im.screen.chat.ChatScreen
import com.linku.im.screen.introduce.ProfileScreen
import com.linku.im.screen.main.MainScreen
import com.linku.im.screen.query.QueryScreen
import com.linku.im.screen.sign.LoginScreen
import com.linku.im.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val _vm: LinkUViewModel by viewModels()

    private lateinit var request: NetworkRequest
    private lateinit var networkCallback: NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        vm = _vm
        WindowCompat.setDecorFitsSystemWindows(window, false)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var initSessionJob: Job? = null

        networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Toast.makeText(this@MainActivity, "网络连接已恢复", Toast.LENGTH_SHORT).show()
                initSessionJob = with(vm) {
                    Authenticator.observeCurrent
                        .onEach { userId ->
                            if (userId == null) LinkUEvent.Disconnect
                            else onEvent(LinkUEvent.InitSession)
                        }
                        .launchIn(lifecycleScope)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Toast.makeText(this@MainActivity, "网络连接已断开", Toast.LENGTH_SHORT).show()
                initSessionJob?.cancel()
            }

        }
        request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        setContent { App() }

        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return vm.state.value.isReady.ifTrue {
                    content.viewTreeObserver.removeOnPreDrawListener(this)
                    true
                } ?: false
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}

lateinit var vm: LinkUViewModel
    private set

@Composable
fun App() {
    val state by vm.state
    val isDarkMode = state.isDarkMode
    @OptIn(ExperimentalAnimationApi::class)
    AppTheme(
        useDarkTheme = isDarkMode,
        enableDynamic = state.dynamicEnabled
    ) {
        val systemUiController = rememberSystemUiController()
        val navigationBarColor = MaterialTheme.colorScheme.surface
        val useDarkIcon = !vm.state.value.isDarkMode

        LaunchedEffect(isDarkMode) {
            systemUiController.setStatusBarColor(
                color = Color.Transparent,
                darkIcons = useDarkIcon
            )
            systemUiController.setNavigationBarColor(
                color = navigationBarColor,
                darkIcons = useDarkIcon
            )
        }
        val navController = rememberAnimatedNavController()
        AnimatedNavHost(
            navController = navController,
            startDestination = Screen.MainScreen.route,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 400)
                )
            },
            exitTransition = {
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(durationMillis = 400),
                    transformOrigin = TransformOrigin(0f, 0.5f)
                )
            },
            popEnterTransition = {
                fadeIn()
            },
            popExitTransition = {
                fadeOut()
            }
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
                ),
            ) { entry ->
                ChatScreen(
                    cid = entry.arguments?.getInt("cid") ?: -1
                )
            }
            composable(Screen.LoginScreen.route) { LoginScreen() }
            composable(Screen.ProfileScreen.route) { ProfileScreen() }
            composable(Screen.QueryScreen.route) { QueryScreen() }
        }

        LaunchedEffect(state.navigateUp) {
            state.navigateUp.handle {
                navController.navigateUp()
            }
        }
        LaunchedEffect(state.navigate) {
            state.navigate.handle { route ->
                navController.navigate(route)
            }
        }
    }
}