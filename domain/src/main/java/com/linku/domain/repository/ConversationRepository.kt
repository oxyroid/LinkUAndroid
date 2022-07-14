package com.linku.domain.repository

import com.linku.domain.entity.Conversation
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun observeConversations(): Flow<List<Conversation>>
}