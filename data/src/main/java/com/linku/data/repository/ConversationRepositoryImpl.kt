package com.linku.data.repository

import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.resourceFlow
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.ChatService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val chatService: ChatService
) : ConversationRepository {
    override fun observeConversations(): Flow<List<Conversation>> {
        return try {
            conversationDao.observeConversations()
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override fun fetchConversations(): Flow<Resource<Unit>> = resourceFlow {
        try {
            chatService.getConversationsBySelf()
                .handle { conversations ->
                    conversations.forEach { conversationDao.insert(it.toConversation()) }
                    emitResource(Unit)
                }
                .catch { s, s1 ->
                    emitResource(s, s1)
                }
        } catch (e: Exception) {
            e.printStackTrace()
            emitResource(e.message ?: "", "?")
        }
    }

    override fun observeLatestMessages(cid: Int): Flow<Message> {
        return messageDao.getLatestMessageByCid(cid).filterNotNull()
    }

    override fun queryConversations(
        name: String?,
        description: String?
    ): Flow<Resource<List<Conversation>>> =
        resourceFlow {
            chatService.queryConversations(name, description)
                .handle { conversations -> emitResource(conversations.map { it.toConversation() }) }
                .catch(::emitResource)
        }
}