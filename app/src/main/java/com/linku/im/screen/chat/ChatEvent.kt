package com.linku.im.screen.chat

sealed class ChatEvent {
    data class Initial(val cid: Int) : ChatEvent()
    object SendTextMessage : ChatEvent()
    data class TextChange(val text: String) : ChatEvent()
}
