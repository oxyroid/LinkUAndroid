package com.linku.data.repository

import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.domain.Auth
import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.entity.Message
import com.linku.domain.repository.MessageRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.ChatService
import com.linku.domain.service.ChatSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

class MessageRepositoryImpl(
    private val socketService: ChatSocketService,
    private val chatService: ChatService,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : MessageRepository {
    override suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit> {
        return socketService.initSession(uid, scope).also {
            messageDao.clearStagingMessages()
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
            try {
                chatService.subscribe()
            } catch (_: Exception) {

            }
        }
    }

    override fun incoming(): Flow<List<Message>> = messageDao
        .incoming()
        .onEach {
            debug {
                Log.e(TAG, "Room Messages Update, size = ${it.size}")
            }
        }

    override fun incoming(cid: Int): Flow<List<Message>> = messageDao.incoming(cid)

    override suspend fun closeSession() = socketService.closeSession()

    override suspend fun sendTextMessage(
        cid: Int,
        content: String
    ): Flow<Resource<Unit>> = flow {
        val uuid = UUID.randomUUID().toString()
        try {
            val userId = Auth.currentUID
            checkNotNull(userId) { "Please sign in first." }
            val stagingMessage =
                Message(
                    id = System.currentTimeMillis().toInt(),
                    cid = cid,
                    uid = userId,
                    content = content,
                    type = "text",
                    timestamp = System.currentTimeMillis(),
                    uuid = uuid,
                    sendState = Message.STATE_PENDING
                )
            messageDao.insert(stagingMessage)
            emit(Resource.Loading)
            chatService.sendMessage(cid, content, "text", uuid)
                .handle { message ->
                    val serverUUID = message.uuid
                    val findByUUID = messageDao.findByUUID(serverUUID)
                    if (findByUUID != null && serverUUID == uuid) {
                        messageDao.levelStagingMessage(
                            uuid = uuid,
                            id = message.id,
                            cid = message.cid,
                            timestamp = message.timestamp
                        )
                        emitResource(Unit)
                    } else {
                        emitResource(
                            message = "",
                            code = ""
                        )
                    }
                }
                .failure { message, code ->
                    messageDao.failedStagingMessage(uuid)
                    emitResource(message, code)
                }
                .map { }
        } catch (e: Exception) {
            e.printStackTrace()
            messageDao.failedStagingMessage(uuid)
            emitResource(e.message ?: "Unknown Error", "?")
        }
    }
}