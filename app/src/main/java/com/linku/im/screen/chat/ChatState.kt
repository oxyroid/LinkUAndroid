package com.linku.im.screen.chat

import android.graphics.Bitmap
import android.net.Uri
import com.linku.domain.Event
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message

data class ChatState(
    val title: String = "",
    val cid: Int = -1,
    val type: Int = Conversation.TYPE_PM,
    val text: String = "",
    val uri: Uri? = null,
    val image: Bitmap? = null,
    val loading: Boolean = false,
    val event: Event<String> = Event.Handled(),
    val messages: List<Message> = emptyList(),
    val scrollToBottom: Event<Unit> = Event.Handled(),
    val firstVisibleIndex: Int = 0
)
