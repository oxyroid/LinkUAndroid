package com.linku.data.repository

import com.linku.domain.entity.Conversation
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.room.dao.ConversationDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao
) : ConversationRepository {
    override fun observeConversations(): Flow<List<Conversation>> {
        return try {
            conversationDao.observeConversations()
        }catch (e:Exception){
            e.printStackTrace()
            flow {  }
        }
    }
}