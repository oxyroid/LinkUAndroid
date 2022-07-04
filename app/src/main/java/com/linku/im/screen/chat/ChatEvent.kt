package com.linku.im.screen.chat

import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.SharedFlow

sealed class ChatEvent {
    data class InitChat(val cid: Int, val source: SharedFlow<Message>) : ChatEvent()
    object SendTextMessage : ChatEvent()
    data class TextChange(val text: String) : ChatEvent()
}
