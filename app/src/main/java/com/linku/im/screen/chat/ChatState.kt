package com.linku.im.screen.chat

import com.linku.domain.entity.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)
