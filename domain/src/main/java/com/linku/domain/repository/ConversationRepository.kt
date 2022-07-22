package com.linku.domain.repository

import com.linku.domain.Resource
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeConversations(): Flow<List<Conversation>>
    fun observeLatestMessages(cid: Int): Flow<Message>
    fun fetchConversations(): Flow<Resource<Unit>>
    fun queryConversations(name: String?, description: String?): Flow<Resource<List<Conversation>>>
}