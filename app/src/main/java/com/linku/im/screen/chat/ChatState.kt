package com.linku.im.screen.chat

import android.net.Uri
import androidx.compose.ui.text.input.TextFieldValue
import com.linku.core.wrapper.Event
import com.linku.domain.bean.Emoji
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message

data class ChatState(
    val title: String = "",
    val subTitle: String = "",
    val introduce: String = "",
    val cid: Int = -1,
    val channelDetailLoading: Boolean = false,
    val type: Conversation.Type = Conversation.Type.PM,
    val textFieldValue: TextFieldValue = TextFieldValue(),
    val emojis: List<Emoji> = emptyList(),
    val emojiSpanExpanded: Boolean = false,
    val uri: Uri? = null,
    val repliedMessage: Message? = null,
    val scroll: Event<Int> = Event.Handled(),
    val focusMessageId: Int? = null,
    val shortcutPushing: Boolean = false,
    val shortcutPushed: Boolean = false
)
