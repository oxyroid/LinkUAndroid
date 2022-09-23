package com.linku.im

import com.linku.im.screen.Screen

data class LinkUState(
    val loading: Boolean = false,
    val label: String? = null,
    val isDarkMode: Boolean = false,
    val isEmojiReady: Boolean = false,
    val hasSynced: Boolean = false,
    val currentScreen: Screen = Screen.MainScreen
)
