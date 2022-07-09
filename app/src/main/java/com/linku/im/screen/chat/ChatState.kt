package com.linku.im.screen.chat

import com.linku.domain.Event

data class ChatState(
    val title: String = "",
    val cid: Int = -1,
    val text: String = "",
    val loading: Boolean = false,
    val sending: Boolean = false,
    val event: Event<String> = Event.Handled()
)
