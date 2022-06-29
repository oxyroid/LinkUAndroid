package com.wzk.oss.screen.chat

import com.wzk.domain.entity.Message

data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false
)
