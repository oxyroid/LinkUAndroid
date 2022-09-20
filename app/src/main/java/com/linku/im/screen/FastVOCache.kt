@file:Suppress("unused")
package com.linku.im.screen

import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.im.extension.containsKey
import com.linku.im.extension.lruCacheOf
import com.linku.im.screen.chat.vo.MessageVO
import com.linku.im.screen.main.vo.ConversationVO
import com.thxbrop.suggester.any

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