package com.linku.im.extension

import androidx.compose.material.DrawerState
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun DrawerState.toggle(coroutineScope: CoroutineScope) {
    coroutineScope.launch(Dispatchers.Main) {
        if (isOpen) close()
        else open()
    }
}

operator fun Color.times(alpha: Float): Color {
    return this.copy(alpha = alpha)
}