package com.linku.im.screen.main

import com.linku.domain.entity.Conversation
import com.linku.im.R
import com.linku.im.application

data class MainState(
    val title: String = application.getString(R.string.app_name),
    val conversations: List<ConversationMainUI> = emptyList(),
    val loading: Boolean = true,
    val drawerTitle: String? = null
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
