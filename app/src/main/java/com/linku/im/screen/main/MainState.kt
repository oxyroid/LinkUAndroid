package com.linku.im.screen.main

import com.linku.domain.entity.Conversation
import com.linku.im.R
import com.linku.im.application

data class MainState(
    val title: String = application.getString(R.string.app_name),
    val conversations: List<Conversation> = emptyList(),
    val loading: Boolean = true,
    val drawerTitle: String? = null
)
