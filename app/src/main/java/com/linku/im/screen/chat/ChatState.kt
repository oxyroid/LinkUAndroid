package com.linku.im.screen.chat

import android.net.Uri
import com.linku.domain.Event
import com.linku.domain.bean.Emoji
import com.linku.domain.entity.Conversation
import com.linku.im.screen.chat.vo.MessageVO

data class ChatState(
    val title: String = "",
    val cid: Int = -1,
    val type: Int = Conversation.TYPE_PM,
    val text: String = "",
    val emojis: List<Emoji> = emptyList(),
    val expended: Boolean = false,
    val uri: Uri? = null,
    val loading: Boolean = true,
    val event: Event<String> = Event.Handled(),
    val messages: List<MessageVO> = emptyList(),
    val scrollToBottomEvent: Event<Unit> = Event.Handled(),
    val firstVisibleIndex: Int = 0
)
