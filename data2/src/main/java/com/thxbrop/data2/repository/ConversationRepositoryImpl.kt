package com.thxbrop.data2.repository

import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.resourceFlow
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.service.ConversationService
import com.linku.domain.toResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val conversationService: ConversationService
) : ConversationRepository {
    override fun observeConversation(cid: Int): Flow<Conversation> = runCatching {
        conversationDao.observeConversation(cid)
    }
        .getOrNull()
        ?: flow { }

    override fun observeConversations(): Flow<List<Conversation>> = kotlin.runCatching {
        conversationDao.observeConversations()
    }
        .getOrNull()
        ?: flow { }

    override fun fetchConversation(cid: Int): Flow<Resource<Unit>> = resourceFlow {
        runCatching {
            conversationService.getConversationById(cid).toResult()
                .onSuccess { conversation ->
                    // TODO save different type conversations
                    if (conversationDao.getById(conversation.id) == null) {
                        conversationDao.insert(conversation.toConversation())
                    }
                    emitResource(Unit)
                }
                .onFailure {
                    emitResource(it.message)
                }
        }.onFailure {
            emitResource(it.message)
        }
    }

    override fun fetchConversations(): Flow<Resource<Unit>> = resourceFlow {
        runCatching {
            conversationService.getConversationsBySelf().toResult()
                .onSuccess { conversations ->
                    conversations.forEach { conversationDao.insert(it.toConversation()) }
                    emitResource(Unit)
                }
                .onFailure {
                    emitResource(it.message)
                }
        }.onFailure {
            emitResource(it.message)
        }
    }


    override fun queryConversations(
        name: String?,
        description: String?
    ): Flow<Resource<List<Conversation>>> = resourceFlow {
        runCatching {
            conversationService.queryConversations(name, description).toResult()
                .onSuccess { conversations -> emitResource(conversations.map { it.toConversation() }) }
                .onFailure { emitResource(it.message) }
        }.onFailure {
            emitResource(it.message)
        }
    }
}