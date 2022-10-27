package com.linku.im.screen.main

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.linku.im.appyx.target.NavTarget

internal sealed class Selection(
    @StringRes open val resId: Int,
    open val icon: ImageVector
) {
    data class Route(
        override val resId: Int,
        val target: NavTarget,
        override val icon: ImageVector
    ) : Selection(resId, icon)

    data class Switch(
        override val resId: Int,
        val value: Boolean,
        val onIcon: ImageVector,
        val offIcon: ImageVector = onIcon,
        val onClick: () -> Unit,
        val onLongClick: () -> Unit = {}
    ) : Selection(resId, if (value) onIcon else offIcon)

    data class Button(
        override val resId: Int,
        override val icon: ImageVector,
        val onClick: () -> Unit,
        val onLongClick: () -> Unit = {}
    ) : Selection(resId, icon)
}
