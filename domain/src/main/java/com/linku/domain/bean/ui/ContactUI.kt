package com.linku.domain.bean.ui

import com.linku.domain.entity.Conversation

data class ContactUI(
    val id: Int,
    val username: String,
    var content: String,
    var image: String,
    var unreadCount: Int = 0,
    var updatedAt: Long = 0,
    var pinned: Boolean = false
)

fun Conversation.toContactUI(
    username: String = name,
    content: String = description,
    image: String = avatar,
    unreadCount: Int = 0
) = ContactUI(
    id, username, content, image, unreadCount, updatedAt, pinned
)