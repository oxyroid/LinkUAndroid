package com.linku.im.screen

import androidx.compose.runtime.Immutable
import com.linku.domain.bean.ui.ContactRequestUI
import com.linku.domain.bean.ui.ContactUI
import com.linku.domain.bean.ui.ConversationUI
import com.linku.domain.bean.ui.MessageUI
import com.linku.domain.entity.ContactRequest
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.domain.entity.User

@Immutable
data class ConversationUIList(
    val value: List<ConversationUI> = emptyList()
)

@Immutable
data class ContactUIList(
    val value: List<ContactUI> = emptyList()
)

@Immutable
data class ConversationList(
    val value: List<Conversation> = emptyList()
)

@Immutable
data class UserList(
    val value: List<User> = emptyList()
)

@Immutable
data class ContactRequestList(
    val value: List<ContactRequest> = emptyList()
)

@Immutable
data class ContactRequestUIList(
    val value: List<ContactRequestUI> = emptyList()
)

@Immutable
data class MessageList(
    val value: List<Message> = emptyList()
)

@Immutable
data class MessageUIList(
    val value: List<MessageUI> = emptyList()
)
