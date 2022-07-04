package com.linku.im.screen.profile.composable

import androidx.compose.ui.graphics.vector.ImageVector
import com.linku.im.screen.Screen

sealed class Setting {
    data class Folder(
        val label: String,
        val icon: ImageVector,
        val screen: Screen
    ) : Setting()

    data class Entity(
        val key: String,
        val value: String?,
        val onClick: () -> Unit
    ) : Setting()
}


