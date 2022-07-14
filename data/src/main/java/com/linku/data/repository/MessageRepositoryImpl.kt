package com.linku.data.repository

import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MessageRepositoryImpl(
    private val socketService: ChatSocketService,
    private val chatService: ChatService,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : MessageRepository {
    override suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit> {
        return socketService.initSession(uid, scope)
    }

    private var _incoming: Flow<List<Message>>? = null
    override fun incoming(): Flow<List<Message>> = _incoming
        ?: messageDao
            .incoming()
            .onEach {
                debug {
                    Log.e(TAG, "Room Messages Update, size = ${it.size}")
                }
            }
            .also {
                _incoming = it
            }

    private var isSyncing = false
    override fun persistence(scope: CoroutineScope) {
        if (isSyncing) return
        try {
            socketService.incoming()
                .onEach { message ->
                    debug {
                        Log.e(TAG, "Message Received: ${message.content}")
                    }
                    messageDao.insert(message)
                    val cid = message.cid
                    if (conversationDao.getById(cid) == null) {
                        chatService.getById(cid).handle { conversation ->
                            conversationDao.insert(conversation)
                        }
                    }
                }
                .launchIn(scope)
            isSyncing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun closeSession() = socketService.closeSession()

    override suspend fun sendTextMessage(cid: Int, content: String): Result<Unit> = sandbox {
        chatService.sendMessage(cid, content, "text")
    }

    override suspend fun subscribe(): Result<Unit> = sandbox { chatService.subscribe() }
}