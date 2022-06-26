package com.wzk.oss.screen.main

import com.wzk.domain.entity.Conversation

data class MainState(
    val title: String,
    val conversations: List<Conversation>
)
