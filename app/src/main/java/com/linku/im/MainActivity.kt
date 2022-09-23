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