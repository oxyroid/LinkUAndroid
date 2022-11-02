package com.linku.im.screen.main

import com.linku.domain.bean.ui.ContactUI
import com.linku.domain.bean.ui.ConversationUI
import com.linku.domain.entity.ContactRequest


data class MainState(
    val conversations: List<ConversationUI> = emptyList(),
    val contracts: List<ContactUI> = emptyList(),
    val requests: List<ContactRequest> = emptyList(),
    val loadingConversations: Boolean = true,
    val loadingMessages: Boolean = true
)
