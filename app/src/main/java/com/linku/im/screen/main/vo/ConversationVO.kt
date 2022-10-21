package com.linku.im.screen.main.vo

import com.linku.domain.entity.Conversation

data class ConversationVO(
    val id: Int,
    val name: String,
    var content: String = "",
    var image: String,
    var unreadCount: Int = 0,
    var updatedAt: Long = 0,
    var pinned: Boolean = false,
) : Comparable<ConversationVO> {
    override fun compareTo(other: ConversationVO): Int = run {
        if (pinned == other.pinned) (other.updatedAt - updatedAt).toInt()
        else {
            if (pinned) 1 else 0
        }
    }
}

internal fun Conversation.toMainUI(
    content: String = "",
    unreadCount: Int = 0
) = ConversationVO(
    id = this.id,
    name = this.name,
    content = content,
    image = this.avatar,
    unreadCount = unreadCount,
    updatedAt = updatedAt,
    pinned = pinned
)
