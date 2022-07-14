package com.linku.data.repository

import com.linku.domain.Resource
import com.linku.domain.Result
import com.linku.domain.entity.Message
import com.linku.domain.repository.MessageRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.sandbox
import com.linku.domain.service.ChatService
import com.linku.domain.service.ChatSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

class MessageRepositoryImpl(
    private val socketService: ChatSocketService,
    private val chatService: ChatService,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : MessageRepository {
    override suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit> {
        return socketService.initSession(uid, scope)
    }

    override fun observeMessages(scope: CoroutineScope): Flow<List<Message>> {
        return try {
            flow {
                emitAll(messageDao.observeMessages())
                socketService.observeMessages()
                    .onEach { message ->
                        messageDao.insert(message)
                        val cid = message.cid
                        if (conversationDao.getById(cid) == null) {
                            chatService.getById(cid).handle { conversation ->
                                conversationDao.insert(conversation)
                            }
                        }
                    }
                    .launchIn(scope)
            }
        }catch (e:Exception){
            e.printStackTrace()
            flow {  }
        }
    }

    override fun observeMessagesByCid(cid: Int): Flow<List<Message>> {
        return try {
            flow {
                emitAll(messageDao.observeMessagesByCid(cid))
            }
        }catch (e: Exception){
            e.printStackTrace()
            flow {  }
        }
    }

    override suspend fun closeSession() {
        socketService.closeSession()
    }

    override suspend fun sendTextMessage(cid: Int, content: String): Result<Unit> = sandbox {
        chatService.sendMessage(cid, content, "text")
    }

    override suspend fun subscribe(): Result<Unit> = sandbox {  chatService.subscribe() }
}