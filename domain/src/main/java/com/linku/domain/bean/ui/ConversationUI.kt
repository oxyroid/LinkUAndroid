package com.linku.domain.bean.ui

import com.linku.domain.entity.Conversation

data class ConversationUI(
    val id: Int,
    val name: String,
    var content: String = "",
    var image: String,
    var unreadCount: Int = 0,
    var updatedAt: Long = 0,
    var pinned: Boolean = false,
)

fun Conversation.toUI(
    content: String = "",
    unreadCount: Int = 0
) = ConversationUI(
    id = this.id,
    name = this.name,
    content = content,
    image = this.avatar,
    unreadCount = unreadCount,
    updatedAt = updatedAt,
    pinned = pinned
)
