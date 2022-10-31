package com.linku.domain.repository

import com.linku.core.wrapper.Resource
import com.linku.domain.Strategy
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Member
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    suspend fun findConversation(cid: Int, strategy: Strategy): Conversation?
    fun observeConversation(cid: Int): Flow<Conversation>
    fun observeConversations(): Flow<List<Conversation>>
    fun fetchConversation(cid: Int): Flow<Resource<Unit>>
    fun fetchConversations(): Flow<Resource<Unit>>
    fun fetchMembers(cid: Int): Flow<Resource<List<Member>>>
    fun queryConversations(
        name: String?,
        description: String?
    ): Flow<Resource<List<Conversation>>>

    suspend fun pin(cid: Int)
}
