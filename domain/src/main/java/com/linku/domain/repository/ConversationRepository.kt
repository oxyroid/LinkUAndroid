package com.linku.domain.repository

import com.linku.domain.Strategy
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Member
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    suspend fun findConversation(cid: Int, strategy: Strategy): Conversation?
    fun observeConversation(cid: Int): Flow<Conversation>
    fun observeConversations(): Flow<List<Conversation>>
    fun fetchConversation(cid: Int): Flow<com.linku.core.wrapper.Resource<Unit>>
    fun fetchConversations(): Flow<com.linku.core.wrapper.Resource<Unit>>
    fun fetchMembers(cid: Int): Flow<com.linku.core.wrapper.Resource<List<Member>>>
    fun queryConversations(
        name: String?,
        description: String?
    ): Flow<com.linku.core.wrapper.Resource<List<Conversation>>>

    suspend fun pin(cid: Int)
}
