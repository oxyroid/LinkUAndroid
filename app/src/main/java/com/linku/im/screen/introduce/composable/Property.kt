package com.linku.im.screen.introduce.composable

import androidx.compose.ui.graphics.vector.ImageVector

sealed class Property {
    data class Folder(
        val label: String,
        val icon: ImageVector
    ) : Property()

    data class Data(
        val key: String,
        val value: CharSequence? = null,
        val actions: List<Action> = emptyList()
    ) : Property() {
        data class Action(
            val text: String,
            val icon: ImageVector,
            val onClick: () -> Unit,
        )
    }
}


