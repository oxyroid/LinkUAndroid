package com.linku.im.screen.chat

import androidx.compose.ui.geometry.Rect

sealed class ChatScreenMode {
    object Messages : ChatScreenMode()
    data class ImageDetail(
        val url: String,
        val boundaries: Rect,
        val aspectRatio: Float = 4 / 3f
    ) : ChatScreenMode()

    data class MemberDetail(val mid: Int) : ChatScreenMode()
    object ChannelDetail : ChatScreenMode()
}