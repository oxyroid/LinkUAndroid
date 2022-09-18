package com.linku.im.screen.chat

sealed class ChatScreenMode {
    object Messages : ChatScreenMode()
    data class MessageDetail(val mid: Int) : ChatScreenMode()
    data class MemberDetail(val mid: Int) : ChatScreenMode()
    object ChannelDetail : ChatScreenMode()
}