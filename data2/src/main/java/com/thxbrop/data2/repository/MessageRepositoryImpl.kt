package com.thxbrop.data2.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.linku.domain.*
import com.linku.domain.bean.CachedFile
import com.linku.domain.bean.StagingMessage
import com.linku.domain.entity.*
import com.linku.domain.repository.MessageRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.ConversationService
import com.linku.domain.service.FileService
import com.linku.domain.service.MessageService
import com.linku.fs_android.writeFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import kotlin.Result

class MessageRepositoryImpl @Inject constructor(
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    @ApplicationContext private val context: Context,
    private val fileService: FileService,
    private val json: Json,
    private val authenticator: Authenticator
) : MessageRepository {
    override fun incoming(): Flow<List<Message>> =
        // Cached Message is original type which is unreadable.
        messageDao.incoming()
            .map { list ->
                list.mapNotNull { message ->
                    // So we will convert each one to readable type.
                    when (val readable = message.toReadable()) {
                        // If it is the message which contains image.
                        is ImageMessage -> {
                            val url = readable.url
                            // If its image content is map to cached file.
                            if (url.startsWith("file:///")) {
                                val uri = Uri.parse(url)
                                val file = uri.toFile()
                                // If it is not exists
                                if (!file.exists()) {
                                    // Fetch latest one from server then update local one.
                                    getMessageById(readable.id, Strategy.NetworkThenCache)
                                        .also {
                                            // If the server one is not exists, we delete it from local.
                                            if (it == null) {
                                                messageDao.delete(readable.id)
                                            }
                                        }
                                } else readable
                                // If its image content is map to ContentProvider
                                // The else-if branch is made for old version
                            } else readable
                        }
                        // Same with Image Message.
                        is GraphicsMessage -> {
                            val url = readable.url
                            if (url.startsWith("file:///")) {
                                val uri = Uri.parse(url)
                                val file = uri.toFile()
                                if (!file.exists()) {
                                    getMessageById(readable.id, Strategy.NetworkThenCache)
                                        .also {
                                            if (it == null) {
                                                messageDao.delete(readable.id)
                                            }
                                        }
                                } else readable
                            } else readable
                        }
                        else -> readable
                    }
                }
            }

    override fun incoming(cid: Int): Flow<List<Message>> = messageDao
        .incoming(cid)
        .map { list ->
            list.mapNotNull { message ->
                // So we will convert each one to readable type.
                when (val readable = message.toReadable()) {
                    is TextMessage -> readable
                    // If it is the message which contains image.
                    is ImageMessage -> {
                        val url = readable.url
                        // If its image content is map to cached file.
                        if (url.startsWith("file:///")) {
                            val uri = Uri.parse(url)
                            val file = uri.toFile()
                            // If it is not exists
                            if (!file.exists()) {
                                // Fetch latest one from server then update local one.
                                getMessageById(readable.id, Strategy.NetworkThenCache)
                                    .also {
                                        // If the server one is not exists, we delete it from local.
                                        if (it == null) {
                                            messageDao.delete(readable.id)
                                        }
                                    }
                            } else readable
                            // If its image content is map to ContentProvider
                            // The else-if branch is made for old version
                        } else readable
                    }
                    // Same with Image Message.
                    is GraphicsMessage -> {
                        val url = readable.url
                        if (url.startsWith("file:///")) {
                            val uri = Uri.parse(url)
                            val file = uri.toFile()
                            if (!file.exists()) {
                                getMessageById(readable.id, Strategy.NetworkThenCache)
                                    .also {
                                        if (it == null) {
                                            messageDao.delete(readable.id)
                                        }
                                    }
                            } else readable
                        } else readable
                    }
                    // Filter not supported message.
                    else -> null
                }
            }
        }

    override fun observeLatestMessages(cid: Int): Flow<Message> {
        return messageDao.getLatestMessageByCid(cid).filterNotNull().map { it.toReadable() }
    }

    private val memory = mutableMapOf<Int, Message?>()
    override suspend fun getMessageById(mid: Int, strategy: Strategy): Message? = when (strategy) {
        Strategy.CacheElseNetwork -> run {
            messageDao.getById(mid)
                ?: messageService.getMessageById(mid)
                    .toResult()
                    .getOrNull()
                    ?.toMessage()
                    ?.also { messageDao.insert(it) }
        }
        Strategy.Memory -> {
            memory.getOrPut(mid) {
                messageService.getMessageById(mid)
                    .toResult()
                    .getOrNull()
                    ?.toMessage()
                    ?.also { messageDao.insert(it) }
            }
        }
        Strategy.NetworkThenCache -> {
            messageService.getMessageById(mid)
                .toResult()
                .getOrNull()
                ?.toMessage()
                ?.also { messageDao.insert(it) }
        }
        Strategy.OnlyCache -> messageDao.getById(mid)
        Strategy.OnlyNetwork -> messageService.getMessageById(mid)
            .toResult()
            .getOrNull()
            ?.toMessage()
    }?.toReadable()


    override suspend fun sendTextMessage(
        cid: Int,
        text: String,
        reply: Int?
    ): Flow<Resource<Unit>> = channelFlow {
        // We wanna to custom the catch block, so we didn't use resourceFlow.
        val userId = authenticator.currentUID ?: run {
            trySend(Resource.Failure("Please sign in first."))
            return@channelFlow
        }
        // 1: Create a staging message.
        val staging = StagingMessage.Text(
            cid = cid,
            uid = userId,
            text = text,
            reply = reply
        )
        // 2: Put the message into database.
        createStagingMessage(staging)
        launch {
            trySend(Resource.Loading)
            // 3: Make real HTTP-Connection to send message.
            val content = json.encodeToString(TextContent(text, reply))
            messageService.sendMessage(
                cid,
                content,
                Message.Type.Text.toString(),
                staging.uuid
            )
                .toResult()
                .onSuccess {
                    // 4. If it is succeed, level-up the staging message by server-message.
                    with(it) {
                        levelStagingMessage(uuid, id, cid, timestamp, content)
                    }
                    trySend(Resource.Success(Unit))
                }.onFailure {
                    // 5. Else downgrade it.
                    launch {
                        downgradeStagingMessage(staging.uuid)
                        trySend(Resource.Failure(it.message))
                    }
                }
        }

    }

    override fun sendImageMessage(
        cid: Int,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>> = channelFlow {
        // 1. Make real HTTP-Connection to upload file.
        val userId = authenticator.currentUID
        checkNotNull(userId) { "Please sign in first." }
        // 2. Create a staging message.
        val staging = StagingMessage.Image(
            cid = cid,
            uid = userId,
            uri = uri,
            reply = reply
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
                    val cachedFile = resource.data
                    launch {
                        val remoteContent = json.encodeToString(
                            ImageContent(cachedFile.remoteUrl, reply)
                        )
                        val localContent = json.encodeToString(
                            ImageContent(cachedFile.localUri.toString(), reply)
                        )
                        messageService.sendMessage(
                            cid = cid,
                            content = remoteContent,
                            type = Message.Type.Image.toString(),
                            uuid = staging.uuid
                        )
                            .toResult()
                            .onSuccess { serverMessage ->
                                // 4. If it is succeed, level-up the staging message by server-message.
                                levelStagingMessage(
                                    uuid = serverMessage.uuid,
                                    id = serverMessage.id,
                                    cid = cid,
                                    timestamp = serverMessage.timestamp,
                                    content = localContent
                                )
                                trySend(Resource.Success(Unit))
                            }
                            .onFailure {
                                // 5. Else downgrade it.
                                launch {
                                    downgradeStagingMessage(staging.uuid)
                                    trySend(Resource.Failure(it.message))
                                }
                            }

                    }
                }
                is Resource.Failure -> {
                    launch {
                        downgradeStagingMessage(staging.uuid)
                        trySend(Resource.Failure(resource.message))
                    }
                }
            }
        }
            .launchIn(this)

    }

    override fun sendGraphicsMessage(
        cid: Int,
        text: String,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>> =
        channelFlow {

            // 1. Make real HTTP-Connection to upload file.
            val userId = authenticator.currentUID
            checkNotNull(userId) { "Please sign in first." }
            // 2. Create a staging message.
            val staging = StagingMessage.Graphics(
                cid = cid,
                uid = userId,
                text = text,
                uri = uri,
                reply = reply
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
                        val cachedFile = resource.data
                        launch {
                            val remoteContent = json.encodeToString(
                                GraphicsContent(
                                    text,
                                    cachedFile.remoteUrl,
                                    reply
                                )
                            )

                            val localContent = json.encodeToString(
                                GraphicsContent(text, cachedFile.localUri.toString(), reply)
                            )
                            messageService.sendMessage(
                                cid = cid,
                                content = remoteContent,
                                type = Message.Type.Graphics.toString(),
                                uuid = staging.uuid
                            )
                                .toResult()
                                .onSuccess { serverMessage ->
                                    // 4. If it is succeed, level-up the staging message by server-message.
                                    levelStagingMessage(
                                        uuid = serverMessage.uuid,
                                        id = serverMessage.id,
                                        cid = serverMessage.cid,
                                        timestamp = serverMessage.timestamp,
                                        content = localContent
                                    )
                                    trySend(Resource.Success(Unit))
                                }.onFailure {
                                    // 5. Else downgrade it.
                                    launch {
                                        downgradeStagingMessage(staging.uuid)
                                        trySend(Resource.Failure(it.message))
                                    }
                                }
                        }
                    }
                    is Resource.Failure -> {
                        launch {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(resource.message))
                        }
                    }
                }
            }
                .launchIn(this)

        }

    private fun uploadImage(uri: Uri?): Flow<Resource<CachedFile>> = flow {
        if (uri == null) {
            emit(Resource.Failure("upload: uri is null."))
            return@flow
        }

        val file = context.writeFs.put(uri)
        file ?: run {
            emit(Resource.Failure("upload: uri is null."))
            return@flow
        }
        val filename = file.name
        val part = MultipartBody.Part
            .createFormData(
                "file",
                filename,
                RequestBody.create(MediaType.parse("image"), file)
            )
        fileService.upload(part)
            .toResult()
            .onSuccess {
                val cachedFile = CachedFile(file.toUri(), it)
                emitResource(cachedFile)
            }
            .onFailure {
                emitResource(it.message)
            }

    }

    private suspend fun createStagingMessage(staging: StagingMessage) {
        val id = System.currentTimeMillis().toInt()
        val message = when (staging) {
            is StagingMessage.Text -> {
                Message(
                    id = id,
                    cid = staging.cid,
                    uid = staging.uid,
                    content = json.encodeToString(
                        TextContent(
                            text = staging.text,
                            reply = staging.reply
                        )
                    ),
                    type = Message.Type.Text,
                    timestamp = System.currentTimeMillis(),
                    uuid = staging.uuid,
                    sendState = Message.STATE_PENDING
                )
            }
            is StagingMessage.Image -> {
                Message(
                    id = id,
                    cid = staging.cid,
                    uid = staging.uid,
                    content = json.encodeToString(
                        ImageContent(
                            url = staging.uri.toString(),
                            reply = staging.reply
                        )
                    ),
                    type = Message.Type.Image,
                    timestamp = System.currentTimeMillis(),
                    uuid = staging.uuid,
                    sendState = Message.STATE_PENDING
                )
            }
            is StagingMessage.Graphics -> Message(
                id = id,
                cid = staging.cid,
                uid = staging.uid,
                content = json.encodeToString(
                    GraphicsContent(
                        text = staging.text,
                        url = staging.uri.toString(),
                        reply = staging.reply
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

    override suspend fun resendMessage(mid: Int) {
        val message = getMessageById(mid, Strategy.OnlyCache) ?: return
        cancelMessage(mid)
        with(message) {
            when (this) {
                is TextMessage -> sendTextMessage(cid, text, reply)
                is ImageMessage -> sendImageMessage(cid, Uri.parse(url), reply)
                is GraphicsMessage -> sendGraphicsMessage(cid, text, Uri.parse(url), reply)
                else -> {}
            }
        }
    }

    override suspend fun cancelMessage(mid: Int) {
        messageDao.delete(mid)
    }

    private suspend fun downgradeStagingMessage(uuid: String) {
        messageDao.failedStagingMessage(uuid)
    }

    override suspend fun fetchUnreadMessages() = messageService
        .getUnreadMessages()
        .toResult()
        .saveIntoDBIfSuccess()


    override suspend fun fetchMessagesAtLeast(after: Long) = messageService
        .getMessageAfter(after)
        .toResult()
        .saveIntoDBIfSuccess()


    private suspend fun Result<List<MessageDTO>>.saveIntoDBIfSuccess() {
        onSuccess { messages ->
            messages.forEach {
                messageDao.insert(it.toMessage())
                val cid = it.cid
                if (conversationDao.getById(cid) == null) {
                    conversationService.getConversationById(cid).toResult()
                        .onSuccess { conversation ->
                            conversationDao.insert(conversation.toConversation())
                        }
                }
            }
        }
    }
}