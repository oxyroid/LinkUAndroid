package com.linku.im.screen.query

import com.linku.domain.Event
import com.linku.domain.entity.Conversation

data class QueryState(
    val text: String = "",
    val includeDescription: Boolean = false,
    val querying: Boolean = false,
    val message: Event<String> = Event.Handled(),
    val conversations: List<Conversation> = emptyList()
)
