package com.linku.im.screen.main

import androidx.compose.ui.text.input.TextFieldValue
import com.linku.im.screen.*


data class MainState(
    val conversations: ConversationUIList = ConversationUIList(),
    val contracts: ContactUIList = ContactUIList(),
    val requests: ContactRequestUIList = ContactRequestUIList(),
    val loadingConversations: Boolean = true,
    val loadingMessages: Boolean = true,

    val queryText: TextFieldValue = TextFieldValue(),
    val queryTextIsDescription: Boolean = false,
    val queryTextIsEmail: Boolean = false,
    val queryResultConversations: ConversationList = ConversationList(),
    val queryResultUsers: UserList = UserList(),
    val queryResultMessages: MessageList = MessageList()
)
