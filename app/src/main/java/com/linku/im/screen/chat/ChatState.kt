package com.linku.im.screen.chat

import com.linku.domain.Event
import com.linku.domain.entity.Message

data class ChatState(
    val title: String = "",
    val cid: Int? = null,
    val text: String = "",
    val messages: List<Message> = emptyList(),
    val loading: Boolean = false,
    val sending: Boolean = false,
    val event: Event<String> = Event.Handled()
)
