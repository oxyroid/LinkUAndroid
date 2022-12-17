package com.linku.im

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.bumble.appyx.core.integration.NodeHost
import com.bumble.appyx.core.integrationpoint.NodeActivity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.linku.im.nav.node.RootNode
import com.linku.im.ui.theme.AppTheme
import com.linku.im.ui.theme.LocalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : NodeActivity(), ViewTreeObserver.OnPreDrawListener {
    private val _vm: LinkUViewModel by viewModels()
    private lateinit var content: View

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        vm = _vm
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme(
                useDarkTheme = vm.readable.isDarkMode
            ) {
                val systemUiController = rememberSystemUiController()
                val theme = LocalTheme.current
                LaunchedEffect(theme) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = theme.isDarkText
                    )
                }
                NodeHost(integrationPoint = appyxIntegrationPoint) {
                    RootNode(it)
                }
            }
        }
        content = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(this)
    }

    override fun onPreDraw(): Boolean {
        val splashCompleted = with(vm.readable) { isEmojiReady && isThemeReady }
        if (splashCompleted) {
            content.viewTreeObserver.removeOnPreDrawListener(this)
        }
        return splashCompleted
    }

    override fun onDestroy() {
        super.onDestroy()
        _vm.onEvent(LinkUEvent.Disconnect)
    }
}

lateinit var vm: LinkUViewModel
    private set
