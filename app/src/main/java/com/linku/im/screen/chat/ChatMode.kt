package com.linku.im.screen.chat

import androidx.compose.ui.geometry.Rect

sealed class ChatMode {
    object Messages : ChatMode()
    data class ImageDetail(
        val url: String,
        val boundaries: Rect,
        val aspectRatio: Float = 4 / 3f
    ) : ChatMode()

    data class MemberDetail(val mid: Int) : ChatMode()
    object ChannelDetail : ChatMode()
}
