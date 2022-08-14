package com.linku.im.screen.chat

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.linku.domain.bean.Emoji
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.im.screen.chat.vo.MessageVO

data class ChatState(
    val title: String = "",
    val cid: Int = -1,
    val type: Conversation.Type = Conversation.Type.PM,
    val textFieldValue: TextFieldValue = TextFieldValue(),
    val emojis: List<Emoji> = emptyList(),
    val expended: Boolean = false,
    val uri: Uri? = null,
    val loading: Boolean = true,
    val messages: List<MessageVO> = emptyList(),
    val firstVisibleIndex: Int = 0,
    val offset: Int = 0,
    val repliedMessage: Message? = null,
    val visitImage: String = ""
)
