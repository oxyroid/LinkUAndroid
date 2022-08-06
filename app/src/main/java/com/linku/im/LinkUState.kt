package com.linku.im

import com.linku.domain.Event
import com.linku.im.screen.Screen

data class LinkUState(
    val loading: Boolean = false,
    val navigateUp: Event<Unit> = Event.Handled(),
    val navigate: Event<String> = Event.Handled(),
    val label: String = "",
    val isDarkMode: Boolean = false,
    val isReady: Boolean = false,
    val dynamicEnabled: Boolean = false,
    val currentScreen: Screen = Screen.MainScreen
)
