package com.linku.im.screen.chat

import androidx.compose.ui.geometry.Rect

sealed class ChatScreenMode {
    object Messages : ChatScreenMode()
    data class ImageDetail(val url: String, val boundaries: Rect) : ChatScreenMode()
    data class MemberDetail(val mid: Int) : ChatScreenMode()
    object ChannelDetail : ChatScreenMode()
}