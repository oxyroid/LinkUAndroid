package com.linku.im.screen.main

import com.linku.domain.entity.Conversation

data class MainState(
    val conversations: List<ConversationMainUI> = emptyList(),
    val contracts: List<ConversationMainUI> = emptyList(),
    val loadingConversations: Boolean = true,
    val loadingMessages: Boolean = true
) {
    data class ConversationMainUI(
        val id: Int,
        val name: String,
        val content: String = "",
        val image: String,
        val unreadCount: Int = 0
    )
}

internal fun Conversation.toMainUI(
    content: String = "",
    unreadCount: Int = 0
) = MainState.ConversationMainUI(
    id = this.id,
    name = this.name,
    content = content,
    image = this.avatar,
    unreadCount = unreadCount
)
