package com.linku.im.screen.main.vo

import com.linku.domain.entity.Conversation

data class ConversationVO(
    val id: Int,
    val name: String,
    val content: String = "",
    val image: String,
    val unreadCount: Int = 0,
    val updatedAt: Long = 0
)

internal fun Conversation.toMainUI(
    content: String = "",
    unreadCount: Int = 0
) = ConversationVO(
    id = this.id,
    name = this.name,
    content = content,
    image = this.avatar,
    unreadCount = unreadCount,
    updatedAt = updatedAt
)
