@file:Suppress("unused")

package com.linku.im.screen

import com.linku.domain.bean.MessageVO
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.im.ktx.containsKey
import com.linku.im.ktx.dsl.any
import com.linku.im.ktx.lruCacheOf
import com.linku.im.screen.main.vo.ConversationVO

object FastVOCache {
    val messages = lruCacheOf<Int, MessageVO>()
    val conversations = lruCacheOf<Int, ConversationVO>()
    inline fun getOrPutMessage(message: Message, block: () -> MessageVO?): MessageVO? = when {
        any {
            suggest { !messages.containsKey(message.id) }
            suggest { messages[message.id]?.message?.timestamp != message.timestamp }
        } -> block()?.let { messages.put(message.id, it) }

        else -> messages[message.id]
    }

    inline fun getOrPutConversation(
        conversation: Conversation,
        block: () -> ConversationVO?
    ): ConversationVO? = when {
        any {
            suggest { !conversations.containsKey(conversation.id) }
            suggest { conversations[conversation.id]?.updatedAt != conversation.updatedAt }
        } -> block()?.let { conversations.put(conversation.id, it) }

        else -> conversations[conversation.id]
    }
}
