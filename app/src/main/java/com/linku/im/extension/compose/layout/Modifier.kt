package com.linku.im.extension.compose.layout

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

fun Modifier.intervalClickable(
    enabled: Boolean = true,
    interval: Long = 800,
    onClick: () -> Unit
): Modifier = composed {
    var rememberedTime = remember { 0L }
    Modifier.clickable(enabled) {
        if (System.currentTimeMillis() - rememberedTime >= interval) {
            rememberedTime = System.currentTimeMillis()
            onClick()
        }
    }
}
