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
import com.linku.im.appyx.RootNode
import com.linku.im.ktx.ifTrue
import com.linku.im.ui.theme.AppTheme
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
//        setContentView(R.layout.activity_main)
        setContent {
            AppTheme(
                useDarkTheme = vm.readable.isDarkMode
            ) {
                val systemUiController = rememberSystemUiController()
                LaunchedEffect(vm.readable.isDarkMode) {
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = !vm.readable.isDarkMode
                    )
                }
                NodeHost(integrationPoint = integrationPoint) {
                    RootNode(it)
                }
            }
        }
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