package com.linku.im.screen.main

import com.linku.im.screen.main.vo.ConversationVO

data class MainState(
    val conversations: List<ConversationVO> = emptyList(),
    val contracts: List<ConversationVO> = emptyList(),
    val loadingConversations: Boolean = true,
    val loadingMessages: Boolean = true
)