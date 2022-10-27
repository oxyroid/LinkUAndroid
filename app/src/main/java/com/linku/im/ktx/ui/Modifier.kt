package com.linku.im.ktx.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.intervalClickable(
    enabled: Boolean = true,
    interval: Long = 800,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit
): Modifier = composed {
    var rememberedTime = remember { 0L }
    val feedback = LocalHapticFeedback.current
    Modifier
        .combinedClickable(
            enabled = enabled,
            onClick = {
                if (System.currentTimeMillis() - rememberedTime >= interval) {
                    rememberedTime = System.currentTimeMillis()
                    onClick()
                }
            },
            onLongClick = {
                if (System.currentTimeMillis() - rememberedTime >= interval) {
                    rememberedTime = System.currentTimeMillis()
                    onLongClick()
                    feedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
        )
}
