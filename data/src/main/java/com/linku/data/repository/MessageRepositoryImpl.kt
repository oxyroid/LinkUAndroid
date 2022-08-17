package com.linku.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.data.error
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
import javax.inject.Inject

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
    override suspend fun getMessageById(mid: Int, strategy: Strategy): Message? {
        return try {
            when (strategy) {
                Strategy.CacheElseNetwork -> run {
                    messageDao.getById(mid)
                        ?: messageService.getMessageById(mid)
                            .peekOrNull()
                            ?.toMessage()
                            ?.also { messageDao.insert(it) }
                }
                Strategy.Memory -> {
                    memory.getOrPut(mid) {
                        messageService.getMessageById(mid)
                            .peekOrNull()
                            ?.toMessage()
                            ?.also { messageDao.insert(it) }
                    }
                }
                Strategy.NetworkThenCache -> {
                    messageService.getMessageById(mid)
                        .peekOrNull()
                        ?.toMessage()
                        ?.also { messageDao.insert(it) }
                }
                Strategy.OnlyCache -> messageDao.getById(mid)
                Strategy.OnlyNetwork -> messageService.getMessageById(mid).peekOrNull()?.toMessage()
            }?.toReadable()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    override suspend fun sendTextMessage(
        cid: Int,
        text: String,
        reply: Int?
    ): Flow<Resource<Unit>> =
        channelFlow {
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
                try {
                    // 3: Make real HTTP-Connection to send message.
                    val content = json.encodeToString(TextContent(text, reply))
                    messageService.sendMessage(
                        cid,
                        content,
                        Message.Type.Text.toString(),
                        staging.uuid
                    ).handle {
                        // 4. If it is succeed, level-up the staging message by server-message.
                        with(it) {
                            levelStagingMessage(uuid, id, cid, timestamp, content)
                        }
                        trySend(Resource.Success(Unit))
                    }.catch { message, code ->
                        // 5. Else downgrade it.
                        launch {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(message, code))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch {
                        downgradeStagingMessage(staging.uuid)
                        trySend(Resource.Failure(e.message ?: ""))
                    }
                }
            }

        }

    override fun sendImageMessage(
        cid: Int,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>> = channelFlow {
        try {
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
                            try {
                                val content =
                                    json.encodeToString(ImageContent(cachedFile.remoteUrl, reply))
                                messageService.sendMessage(
                                    cid = cid,
                                    content = content,
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
                                            content = content
                                        )
                                    }
                                    trySend(Resource.Success(Unit))
                                }.catch { message, _ ->
                                    // 5. Else downgrade it.
                                    launch {
                                        downgradeStagingMessage(staging.uuid)
                                        trySend(Resource.Failure(message))
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                launch {
                                    downgradeStagingMessage(staging.uuid)
                                    trySend(Resource.Failure(e.message ?: ""))
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
        } catch (e: Exception) {
            e.printStackTrace()
            trySend(Resource.Failure(e.message ?: ""))
        }

    }

    override fun sendGraphicsMessage(
        cid: Int,
        text: String,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>> =
        channelFlow {
            try {
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
                                try {
                                    val content = json.encodeToString(
                                        GraphicsContent(
                                            text,
                                            cachedFile.remoteUrl,
                                            reply
                                        )
                                    )
                                    messageService.sendMessage(
                                        cid = cid,
                                        content = content,
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
                                                content = content
                                            )
                                        }
                                        trySend(Resource.Success(Unit))
                                    }.catch { message, _ ->
                                        // 5. Else downgrade it.
                                        launch {
                                            downgradeStagingMessage(staging.uuid)
                                            trySend(Resource.Failure(message))
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    launch {
                                        downgradeStagingMessage(staging.uuid)
                                        trySend(Resource.Failure(e.message ?: ""))
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
            } catch (e: Exception) {
                e.printStackTrace()
                trySend(Resource.Failure(e.message ?: ""))
            }

        }

    private fun uploadImage(uri: Uri?): Flow<Resource<CachedFile>> =
        resourceFlow {
            if (uri == null) {
                debug { Log.e(TAG, "upload: uri is null.") }
                emitOldVersionResource()
                return@resourceFlow
            }

            val uuid = UUID.randomUUID().toString()
            val file = File(context.externalCacheDir, "$uuid.png")
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
                                .handle {
                                    val cachedFile =
                                        CachedFile(Uri.fromFile(file), it)
                                    emitResource(cachedFile)
                                }
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

    override suspend fun resendStagingMessage(uuid: String) {
        messageDao.resendStagingMessage(uuid)
    }

    private suspend fun downgradeStagingMessage(uuid: String) {
        messageDao.failedStagingMessage(uuid)
    }

    override suspend fun fetchUnreadMessages() {
        try {
            messageService
                .getUnreadMessages()
                .saveIntoDBIfSuccess()
        } catch (e: Exception) {
            error(e.message, "MessageRepository_fetchUnreadMessages")
        }
    }

    override suspend fun fetchMessagesAtLeast(after: Long) {
        try {
            messageService
                .getMessageAfter(after)
                .saveIntoDBIfSuccess()
        } catch (e: Exception) {
            error(e.message, "MessageRepository_fetchMessagesAtLeast")
        }
    }

    private suspend fun Result<List<MessageDTO>>.saveIntoDBIfSuccess() = handle { messages ->
        messages.forEach {
            messageDao.insert(it.toMessage())
            val cid = it.cid
            if (conversationDao.getById(cid) == null) {
                conversationService.getConversationById(cid).handle { conversation ->
                    conversationDao.insert(conversation.toConversation())
                }
            }
        }
    }
}