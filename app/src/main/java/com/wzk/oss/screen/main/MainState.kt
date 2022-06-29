package com.wzk.oss.screen.main

import com.wzk.domain.entity.Conversation
import com.wzk.oss.R
import com.wzk.oss.application

data class MainState(
    val title: String = application.getString(R.string.app_name),
    val conversations: List<Conversation> = emptyList()
)
