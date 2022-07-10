package com.linku.im.screen.overall

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.linku.im.R
import com.linku.im.application
import com.linku.im.screen.Screen

typealias Content = () -> Unit

data class OverallState(
    val loading: Boolean = false,
    val online: Boolean = false,
    val icon: ImageVector = Icons.Default.Menu,
    val title: String = application.getString(R.string.connecting),
    val navClick: Content = {},
    val actions: @Composable RowScope.() -> Unit = @Composable {},
    val isDarkMode: Boolean = false,
    val currentScreen: Screen = Screen.MainScreen
)
