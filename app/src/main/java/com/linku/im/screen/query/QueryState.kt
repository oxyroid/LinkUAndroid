package com.linku.im.screen.query

import androidx.compose.ui.text.input.TextFieldValue
import com.linku.domain.Event
import com.linku.domain.entity.Conversation

data class QueryState(
    val text: TextFieldValue = TextFieldValue(),
    val isDescription: Boolean = false,
    val querying: Boolean = false,
    val conversations: List<Conversation> = emptyList()
)