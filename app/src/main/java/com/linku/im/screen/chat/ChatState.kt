package com.linku.im.screen.chat

import com.linku.domain.Event
import com.linku.domain.entity.Message

data class ChatState(
    val title: String = "",
    val cid: Int = -1,
    val text: String = "",
    val loading: Boolean = false,
    val event: Event<String> = Event.Handled(),
    val messages: List<Message> = emptyList(),
    val scrollToBottom: Event<Unit> = Event.Handled(),
    val firstVisibleIndex: Int = 0
)
