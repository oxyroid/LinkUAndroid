package com.linku.im.screen.main

import com.linku.domain.bean.ui.ConversationUI


data class MainState(
    val conversations: List<ConversationUI> = emptyList(),
    val contracts: List<ConversationUI> = emptyList(),
    val loadingConversations: Boolean = true,
    val loadingMessages: Boolean = true
)
