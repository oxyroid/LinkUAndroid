package com.linku.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.domain.*
import com.linku.domain.entity.GraphicsContent
import com.linku.domain.entity.Message
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.MessageRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.ChatService
import com.linku.domain.service.FileService
import com.linku.domain.service.NotificationService
import com.linku.domain.service.WebSocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class MessageRepositoryImpl(
    private val socketService: WebSocketService,
    private val chatService: ChatService,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val notificationService: NotificationService,
    private val context: Context,
    private val fileService: FileService,
    private val json: Json,
    private val authenticator: Authenticator
) : MessageRepository {
    private var job: Job? = null
    override fun initSession(uid: Int?): Flow<Resource<Unit>> = channelFlow {
        try {
            job?.cancel()
            job = socketService.initSession(uid)
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            trySend(Resource.Loading)
                            messageDao.clearStagingMessages()
                            socketService.onClosed {
                                debug { Log.e(TAG, "Message Channel Closed!") }
                                if (authenticator.currentUID != null) {
                                    trySend(Resource.Failure("Message Channel Closed!"))
                                }
                            }
                        }
                        is Resource.Failure -> trySend(
                            Resource.Failure(resource.message, resource.code)
                        )
                        is Resource.Success -> {
                            launch {
                                try {
                                    chatService.subscribe()
                                        .handleUnit {
                                            Log.e(TAG, "initSession: mqtt success")
                                            trySend(Resource.Success(Unit))
                                        }
                                        .catch { message, code ->
                                            Log.e(TAG, "initSession: mqtt failed")
                                            trySend(
                                                Resource.Failure(message, code)
                                            )
                                        }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                socketService.incoming()
                                    .collectLatest { message ->
                                        debug {
                                            Log.e(TAG, "Message Received: ${message.content}")
                                        }
                                        notificationService.onCollected(message)
                                        messageDao.insert(message)
                                        val cid = message.cid
                                        if (conversationDao.getById(cid) == null) {
                                            chatService.getById(cid).handle { conversation ->
                                                conversationDao.insert(conversation.toConversation())
                                            }
                                        }
                                    }
                            }
                            launch {
                                fetchUnreadMessages()
                            }
                        }
                    }
                }
                .launchIn(this)
        } catch (e: Exception) {
            trySend(Resource.Failure(e.message ?: ""))
        }
    }

    override fun incoming(): Flow<List<Message>> = messageDao.incoming()

    override fun incoming(cid: Int): Flow<List<Message>> = messageDao.incoming(cid)

    override suspend fun closeSession() = socketService.closeSession()

    override fun getMessageById(mid: Int): Flow<Resource<Message>> = resourceFlow {
        val message = messageDao.getById(mid)
        message?.let { emitResource(it.toReadable()) }
        // FIXME: GET LATEST FROM SERVER
    }

    override suspend fun sendTextMessage(cid: Int, text: String): Flow<Resource<Unit>> =
        channelFlow {
            // We wanna to custom the catch block, so we didn't use resourceFlow.
            val userId = authenticator.currentUID ?: kotlin.run {
                trySend(Resource.Failure("Please sign in first."))
                return@channelFlow
            }
            // 1: Create a staging message.
            val staging = MessageRepository.StagingMessage.Text(
                cid = cid,
                uid = userId,
                text = text
            )
            // 2: Put the message into database.
            createStagingMessage(staging)
            launch {
                trySend(Resource.Loading)
                try {
                    // 3: Make real HTTP-Connection to send message.
                    chatService.sendMessage(
                        cid,
                        text,
                        Message.Type.Text.toString(),
                        staging.uuid
                    ).handle {
                        // 4. If it is succeed, level-up the staging message by server-message.
                        with(it) {
                            levelStagingMessage(uuid, id, cid, timestamp, staging.text)
                        }
                        trySend(Resource.Success(Unit))
                    }.catch { message, code ->
                        // 5. Else downgrade it.
                        downgradeStagingMessage(staging.uuid)
                        trySend(Resource.Failure(message, code))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    downgradeStagingMessage(staging.uuid)
                    trySend(Resource.Failure(e.message ?: ""))
                }
            }

        }

    override fun sendImageMessage(cid: Int, uri: Uri): Flow<Resource<Unit>> = channelFlow {
        try {
            // 1. Make real HTTP-Connection to upload file.
            val userId = authenticator.currentUID
            checkNotNull(userId) { "Please sign in first." }
            // 2. Create a staging message.
            val staging = MessageRepository.StagingMessage.Image(
                cid = cid, uid = userId, uri = uri
            )
            uploadImage(uri).onEach { resource ->
                when (resource) {
                    Resource.Loading -> {
                        // 3. Put the message into database.
                        createStagingMessage(staging)
                        trySend(Resource.Loading)
                    }
                    is Resource.Success -> {
                        // 4. Make real HTTP-Connection to send message.
                        launch {
                            try {
                                chatService.sendMessage(
                                    cid = cid,
                                    content = resource.data,
                                    type = Message.Type.Image.toString(),
                                    uuid = staging.uuid
                                ).handle { serverMessage ->
                                    // 4. If it is succeed, level-up the staging message by server-message.
                                    with(serverMessage) {
                                        levelStagingMessage(
                                            uuid = uuid,
                                            id = id,
                                            cid = cid,
                                            timestamp = timestamp,
                                            content = uri.toString()
                                        )
                                    }
                                    trySend(Resource.Success(Unit))
                                }.catch { message, _ ->
                                    // 5. Else downgrade it.
                                    downgradeStagingMessage(staging.uuid)
                                    trySend(Resource.Failure(message))
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    }
                    is Resource.Failure -> {
                        downgradeStagingMessage(staging.uuid)
                        trySend(Resource.Failure(resource.message))
                    }
                }
            }
                .launchIn(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun sendGraphicsMessage(cid: Int, text: String, uri: Uri): Flow<Resource<Unit>> =
        channelFlow {
            try {
                // 1. Make real HTTP-Connection to upload file.
                val userId = authenticator.currentUID
                checkNotNull(userId) { "Please sign in first." }
                // 2. Create a staging message.
                val staging = MessageRepository.StagingMessage.Graphics(
                    cid = cid, uid = userId, text = text, uri = uri
                )
                uploadImage(uri).onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            // 3. Put the message into database.
                            createStagingMessage(staging)
                            trySend(Resource.Loading)
                        }
                        is Resource.Success -> {
                            // 4. Make real HTTP-Connection to send message.
                            launch {
                                try {
                                    val content = GraphicsContent(text, resource.data)
                                    chatService.sendMessage(
                                        cid = cid,
                                        content = json.encodeToString(content),
                                        type = Message.Type.Graphics.toString(),
                                        uuid = staging.uuid
                                    ).handle { serverMessage ->
                                        // 4. If it is succeed, level-up the staging message by server-message.
                                        with(serverMessage) {
                                            levelStagingMessage(
                                                uuid = uuid,
                                                id = id,
                                                cid = cid,
                                                timestamp = timestamp,
                                                content = GraphicsContent(
                                                    text = text, url = uri.toString()
                                                ).let(json::encodeToString)
                                            )
                                        }
                                        trySend(Resource.Success(Unit))
                                    }.catch { message, _ ->
                                        // 5. Else downgrade it.
                                        downgradeStagingMessage(staging.uuid)
                                        trySend(Resource.Failure(message))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        is Resource.Failure -> {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(resource.message))
                        }
                    }
                }
                    .launchIn(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

    private fun uploadImage(uri: Uri?): Flow<Resource<String>> = resourceFlow {
        if (uri == null) {
            debug { Log.e(TAG, "upload: uri is null.") }
            emitOldVersionResource()
            return@resourceFlow
        }

        val default = UUID.randomUUID().toString()
        val segment = uri.lastPathSegment ?: default
        val file = File(context.cacheDir, "$segment.png")
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
        val resolver = context.contentResolver
        try {
            file.outputStream().use {
                resolver.openInputStream(uri).use { stream ->
                    if (stream != null) {
                        stream.copyTo(it)
                        val filename = file.name
                        val part = MultipartBody.Part
                            .createFormData(
                                "file",
                                filename,
                                RequestBody.create(MediaType.parse("image"), file)
                            )
                        fileService.upload(part)
                            .handle(::emitResource)
                            .catch(::emitResource)
                    } else {
                        debug { Log.e(TAG, "upload: cannot open stream.") }
                        emitOldVersionResource()
                        return@resourceFlow
                    }
                }
            }

        } catch (e: FileNotFoundException) {
            debug { Log.e(TAG, "upload: cannot find file.") }
            emitOldVersionResource()
            return@resourceFlow
        }
    }

    private suspend fun createStagingMessage(staging: MessageRepository.StagingMessage) {
        val id = System.currentTimeMillis().toInt()
        val message = when (staging) {
            is MessageRepository.StagingMessage.Text -> {
                Message(
                    id = id,
                    cid = staging.cid,
                    uid = staging.uid,
                    content = staging.text,
                    type = Message.Type.Text,
                    timestamp = System.currentTimeMillis(),
                    uuid = staging.uuid,
                    sendState = Message.STATE_PENDING
                )
            }
            is MessageRepository.StagingMessage.Image -> Message(
                id = id,
                cid = staging.cid,
                uid = staging.uid,
                content = staging.uri.toString(),
                type = Message.Type.Image,
                timestamp = System.currentTimeMillis(),
                uuid = staging.uuid,
                sendState = Message.STATE_PENDING
            )
            is MessageRepository.StagingMessage.Graphics -> Message(
                id = id,
                cid = staging.cid,
                uid = staging.uid,
                content = json.encodeToString(
                    GraphicsContent(
                        staging.text, staging.uri.toString()
                    )
                ),
                type = Message.Type.Graphics,
                timestamp = System.currentTimeMillis(),
                uuid = staging.uuid,
                sendState = Message.STATE_PENDING
            )
        }
        messageDao.insert(message)
    }

    private suspend fun levelStagingMessage(
        uuid: String, id: Int, cid: Int, timestamp: Long, content: String
    ) {
        messageDao.levelStagingMessage(uuid, id, cid, timestamp, content)
    }

    override suspend fun resendStagingMessage(uuid: String) {
        messageDao.resendStagingMessage(uuid)
    }

    private suspend fun downgradeStagingMessage(uuid: String) {
        messageDao.failedStagingMessage(uuid)
    }

    override suspend fun fetchUnreadMessages() {
        try {
            chatService.getUnreadMessages().handle { messages ->
                messages.forEach {
                    messageDao.insert(it.toMessage())
                    val cid = it.cid
                    if (conversationDao.getById(cid) == null) {
                        chatService.getById(cid).handle { conversation ->
                            conversationDao.insert(conversation.toConversation())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}