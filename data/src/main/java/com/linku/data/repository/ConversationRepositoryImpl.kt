package com.linku.data.repository

import com.linku.domain.*
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.ConversationDTO
import com.linku.domain.entity.Member
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.service.ConversationService
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val conversationService: ConversationService,
    private val json: Json,
    private val mmkv: MMKV
) : ConversationRepository {
    override suspend fun findConversation(cid: Int, strategy: Strategy): Conversation? {
        suspend fun fromBackend(): ConversationDTO? = try {
            resultOf {
                conversationService.getConversationById(cid)
            }
                .getOrNull()
        } catch (e: Exception) {
            null
        }

        suspend fun fromIO(): Conversation? = conversationDao.getById(cid)

        suspend fun ConversationDTO.toIO() = conversationDao.insert(this.toConversation())

        return when (strategy) {
            Strategy.OnlyCache -> fromIO()
            Strategy.OnlyNetwork -> fromBackend()?.toConversation()
            Strategy.Memory -> run {
                fun fromMemory(): ConversationDTO? = mmkv.decodeString("conversation_$cid")?.let {
                    json.decodeFromString<ConversationDTO>(it)
                }

                fun ConversationDTO.toMemory() {
                    mmkv.encode("conversation_$cid", json.encodeToString(this))
                }
                fromMemory()
                    ?.toConversation()
                    ?: fromIO()
                    ?: fromBackend()?.also {
                        it.toIO()
                        it.toMemory()
                    }?.toConversation()
            }

            Strategy.NetworkElseCache -> fromBackend()?.let {
                it.toIO()
                it.toConversation()
            } ?: fromIO()
            Strategy.CacheElseNetwork -> fromIO() ?: fromBackend()?.let {
                it.toIO()
                it.toConversation()
            }
        }
    }

    override fun observeConversation(cid: Int): Flow<Conversation> = runCatching {
        conversationDao.observeConversation(cid)
    }
        .getOrNull()
        ?: flow { }

    override fun observeConversations(): Flow<List<Conversation>> = runCatching {
        conversationDao.observeConversations()
    }
        .getOrNull()
        ?: flow { }

    override fun fetchConversation(cid: Int): Flow<Resource<Unit>> = resourceFlow {
        runCatching {
            resultOf { conversationService.getConversationById(cid) }
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
            resultOf { conversationService.getConversationsBySelf() }
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
            resultOf {
                conversationService
                    .queryConversations(name, description)
            }
                .onSuccess { conversations -> emitResource(conversations.map { it.toConversation() }) }
                .onFailure { emitResource(it.message) }
        }.onFailure {
            emitResource(it.message)
        }
    }

    override fun fetchMembers(cid: Int): Flow<Resource<List<Member>>> = resourceFlow {
        runCatching {
            resultOf {
                conversationService
                    .getMembersByCid(cid)
            }
                .onSuccess { emitResource(it) }
                .onFailure { emitResource(it.message) }
        }.onFailure {
            emitResource(it.message)
        }
    }
}
