package com.linku.im.extension

import androidx.compose.material.DrawerState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

operator fun Color.times(alpha: Float): Color {
    return this.copy(alpha = alpha)
}

inline fun <R> Boolean?.ifTrue(block: () -> R): R? {
    return if (this == true) block()
    else null
}

fun DrawerState.toggle(coroutineScope: CoroutineScope) {
    coroutineScope.launch {
        if (isOpen) close()
        else open()
    }
}