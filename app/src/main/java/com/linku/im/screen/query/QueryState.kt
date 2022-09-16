package com.linku.im.screen.query

import androidx.compose.ui.text.input.TextFieldValue
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.User

data class QueryState(
    val text: TextFieldValue = TextFieldValue(),
    val isDescription: Boolean = false,
    val isEmail: Boolean = false,
    val queryingConversations: Boolean = false,
    val queryingUsers: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val users: List<User> = emptyList()
)