package com.linku.im

import com.linku.im.screen.Screen

data class LinkUState(
    val loading: Boolean = false,
    val label: String? = null,
    val isDarkMode: Boolean = false,
    val isReady: Boolean = false,
    val dynamicEnabled: Boolean = false,
    val currentScreen: Screen = Screen.MainScreen
)
