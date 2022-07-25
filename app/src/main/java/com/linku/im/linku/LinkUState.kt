package com.linku.im.linku

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.linku.domain.Event
import com.linku.im.R
import com.linku.im.applicationContext
import com.linku.im.screen.Screen

typealias Content = () -> Unit

data class LinkUState(
    val loading: Boolean = false,
    val navigateUp: Event<Unit> = Event.Handled(),
    val navigate: Event<String> = Event.Handled(),
    val online: Boolean = false,
    val icon: ImageVector = Icons.Default.Menu,
    val label: String = applicationContext.getString(R.string.connecting),
    val navClick: Content = {},
    val title: @Composable () -> Unit = {},
    val actions: @Composable RowScope.() -> Unit = {},
    val isDarkMode: Boolean = false,
    val dynamicEnabled: Boolean = false,
    val currentScreen: Screen = Screen.MainScreen
)
