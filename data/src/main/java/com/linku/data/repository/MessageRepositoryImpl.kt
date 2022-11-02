package com.linku.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.linku.core.extension.json
import com.linku.core.extension.use
import com.linku.core.util.ImageUtil
import com.linku.core.wrapper.Resource
import com.linku.core.wrapper.resultOf
import com.linku.data.R
import com.linku.domain.Strategy
import com.linku.domain.auth.Authenticator
import com.linku.domain.bean.StagingMessage
import com.linku.domain.entity.GraphicsContent
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageContent
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.Message
import com.linku.domain.entity.MessageDTO
import com.linku.domain.entity.TextContent
import com.linku.domain.entity.TextMessage
import com.linku.domain.repository.FileRepository
import com.linku.domain.repository.FileResource
import com.linku.domain.repository.MessageRepository
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.api.MessageService
import com.tencent.mmkv.MMKV
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageService: MessageService,
    private val messageDao: MessageDao,
    @ApplicationContext private val context: Context,
    private val fileRepository: FileRepository,
    private val mmkv: MMKV,
    private val authenticator: Authenticator
) : MessageRepository {

    companion object {
        private const val SCHEMA_FILE = "file:///"
    }

    override fun incoming(): Flow<List<Message>> {
        return messageDao
            .incoming()
            .map { list ->
                list.mapNotNull { it.fixUri() }
            }
    }

    override fun incoming(cid: Int): Flow<List<Message>> {
        return messageDao
            .incoming(cid)
            .map { list ->
                list.mapNotNull { it.fixUri() }
            }
    }

    private suspend fun Message.fixUri(): Message? {
        return when (val readable = this.toReadable()) {
            is ImageMessage -> {
                val url = readable.url
                if (url.startsWith(SCHEMA_FILE)) {
                    val uri = Uri.parse(url)
                    val file = uri.toFile()
                    if (!file.exists()) {
                        getMessageById(readable.id, Strategy.NetworkElseCache).also {
                            if (it == null) {
                                messageDao.delete(readable.id)
                            }
                        }
                    } else readable
                } else readable
            }

            is GraphicsMessage -> {
                val url = readable.url
                if (url.startsWith(SCHEMA_FILE)) {
                    val uri = Uri.parse(url)
                    val file = uri.toFile()
                    if (!file.exists()) {
                        getMessageById(readable.id, Strategy.NetworkElseCache).also {
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

    override fun observeLatestMessage(cid: Int): Flow<Message> {
        return messageDao
            .observeLatestMessageByCid(cid)
            .filterNotNull()
            .map {
                it.toReadable()
            }
    }

    override suspend fun getMessageById(mid: Int, strategy: Strategy): Message? {
        suspend fun fromBackend(): MessageDTO? = try {
            resultOf {
                messageService.getMessageById(mid)
            }.getOrNull()
        } catch (e: Exception) {
            null
        }

        suspend fun fromIO(): Message? = messageDao.getById(mid)

        suspend fun MessageDTO.toIO() {
            val old = messageDao.getById(this.id)
            if (old == null) messageDao.insert(this.toMessage())
        }

        return when (strategy) {
            Strategy.OnlyCache -> fromIO()
            Strategy.OnlyNetwork -> fromBackend()?.toMessage()
            Strategy.Memory -> run {
                fun fromMemory(): MessageDTO? = mmkv.decodeString("message_$mid")?.let {
                    json.decodeFromString<MessageDTO>(it)
                }

                fun MessageDTO.toMemory() {
                    mmkv.encode("message_$mid", json.encodeToString(this))
                }
                fromMemory()?.toMessage() ?: fromIO() ?: fromBackend()?.also {
                    it.toIO()
                    it.toMemory()
                }?.toMessage()
            }

            Strategy.NetworkElseCache -> fromBackend()?.let {
                it.toIO()
                it.toMessage()
            } ?: fromIO()

            Strategy.CacheElseNetwork -> fromIO() ?: fromBackend()?.let {
                it.toIO()
                it.toMessage()
            }
        }?.toReadable()
    }

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
            resultOf(
                block = {
                    messageService.sendMessage(
                        cid, content, Message.Type.Text.toString(), staging.uuid
                    )
                },
                onError = {
                    downgradeStagingMessage(staging.uuid)
                }
            )
                .onSuccess {
                    // 4. If it is succeed, level-up the staging message by server-message.
                    with(it) {
                        levelStagingMessage(uuid, id, cid, timestamp, content)
                    }
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

    override fun sendImageMessage(
        cid: Int,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>> = channelFlow {
        // 1. Make real HTTP-Connection to upload file.
        val userId = authenticator.currentUID
        checkNotNull(userId) { context.getString(R.string.error_no_auth) }
        // 2. Create a staging message.
        val staging = StagingMessage.Image(
            cid = cid,
            uid = userId,
            uri = uri,
            reply = reply
        )
        fileRepository.uploadImage(uri)
            .onEach { resource ->
                when (resource) {
                    FileResource.Loading -> {
                        // 3. Put the message into database.
                        createStagingMessage(staging)
                        trySend(Resource.Loading)
                    }

                    is FileResource.Success -> {
                        // 4. Make real HTTP-Connection to send message.
                        val cachedFile = resource.data
                        launch {
                            val (width, height) = ImageUtil.decodeBitmap(
                                context.contentResolver,
                                cachedFile.localUri
                            )?.use {
                                it.width to it.height
                            } ?: (-1 to -1)

                            val remoteContent = json.encodeToString(
                                ImageContent(
                                    url = cachedFile.remoteUrl,
                                    reply = reply,
                                    width = width,
                                    height = height
                                )
                            )

                            val localContent = json.encodeToString(
                                ImageContent(
                                    url = cachedFile.localUri.toString(),
                                    reply = reply,
                                    width = width,
                                    height = height
                                )
                            )
                            resultOf(
                                block = {
                                    messageService.sendMessage(
                                        cid = cid,
                                        content = remoteContent,
                                        type = Message.Type.Image.toString(),
                                        uuid = staging.uuid
                                    )
                                },
                                onError = {
                                    downgradeStagingMessage(staging.uuid)
                                }
                            )
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

                    is FileResource.OtherError -> {
                        launch {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(resource.message))
                        }
                    }

                    else -> {
                        val resId = when (resource) {
                            FileResource.FileCannotFoundError -> R.string.error_file_cannot_found
                            FileResource.NullUriError -> R.string.error_null_uri
                            else -> R.string.error_unknown
                        }
                        val msg = context.getString(resId)
                        launch {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(msg))
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
    ): Flow<Resource<Unit>> = channelFlow {
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

        fileRepository.uploadImage(uri)
            .onEach { resource ->
                when (resource) {
                    FileResource.Loading -> {
                        // 3. Put the message into database.
                        createStagingMessage(staging)
                        trySend(Resource.Loading)
                    }

                    is FileResource.Success -> {
                        // 4. Make real HTTP-Connection to send message.
                        val cachedFile = resource.data
                        launch {
                            val (width, height) = ImageUtil.decodeBitmap(
                                context.contentResolver,
                                cachedFile.localUri
                            )?.use {
                                it.width to it.height
                            } ?: (-1 to -1)

                            val remoteContent = json.encodeToString(
                                GraphicsContent(
                                    text = text,
                                    url = cachedFile.remoteUrl,
                                    reply = reply,
                                    width = width,
                                    height = height
                                )
                            )

                            val localContent = json.encodeToString(
                                GraphicsContent(
                                    text = text,
                                    url = cachedFile.localUri.toString(),
                                    reply = reply,
                                    width = width,
                                    height = height
                                )
                            )
                            resultOf(
                                block = {
                                    messageService.sendMessage(
                                        cid = cid,
                                        content = remoteContent,
                                        type = Message.Type.Graphics.toString(),
                                        uuid = staging.uuid
                                    )
                                },
                                onError = {
                                    downgradeStagingMessage(staging.uuid)
                                }
                            )
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

                    is FileResource.OtherError -> {
                        launch {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(resource.message))
                        }
                    }

                    else -> {
                        val resId = when (resource) {
                            FileResource.FileCannotFoundError -> R.string.error_file_cannot_found
                            FileResource.NullUriError -> R.string.error_null_uri
                            else -> R.string.error_unknown
                        }
                        val msg = context.getString(resId)
                        launch {
                            downgradeStagingMessage(staging.uuid)
                            trySend(Resource.Failure(msg))
                        }
                    }
                }
            }
            .launchIn(this)
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
                val (width, height) = ImageUtil.decodeBitmap(
                    context.contentResolver,
                    staging.uri
                )?.use {
                    it.width to it.height
                } ?: (-1 to -1)

                Message(
                    id = id,
                    cid = staging.cid,
                    uid = staging.uid,
                    content = json.encodeToString(
                        ImageContent(
                            url = staging.uri.toString(),
                            reply = staging.reply,
                            width = width,
                            height = height
                        )
                    ),
                    type = Message.Type.Image,
                    timestamp = System.currentTimeMillis(),
                    uuid = staging.uuid,
                    sendState = Message.STATE_PENDING
                )
            }

            is StagingMessage.Graphics -> {
                val (width, height) = ImageUtil.decodeBitmap(
                    context.contentResolver, staging.uri
                )?.use {
                    it.width to it.height
                } ?: (-1 to -1)

                Message(
                    id = id,
                    cid = staging.cid,
                    uid = staging.uid,
                    content = json.encodeToString(
                        GraphicsContent(
                            text = staging.text,
                            url = staging.uri.toString(),
                            reply = staging.reply,
                            width = width,
                            height = height
                        )
                    ),
                    type = Message.Type.Graphics,
                    timestamp = System.currentTimeMillis(),
                    uuid = staging.uuid,
                    sendState = Message.STATE_PENDING
                )
            }
        }
        messageDao.insert(message)
    }

    private suspend fun levelStagingMessage(
        uuid: String, id: Int, cid: Int, timestamp: Long, content: String
    ) {
        messageDao.levelStagingMessage(uuid, id, cid, timestamp, content)
    }

    override suspend fun resendMessage(mid: Int): Flow<Resource<Unit>> {
        val message = getMessageById(mid, Strategy.OnlyCache) ?: return flow { }
        cancelMessage(mid)
        return with(message) {
            when (this) {
                is TextMessage -> sendTextMessage(cid, text, reply)
                is ImageMessage -> sendImageMessage(cid, Uri.parse(url), reply)
                is GraphicsMessage -> sendGraphicsMessage(cid, text, Uri.parse(url), reply)
                else -> flow {}
            }
        }
    }

    override suspend fun cancelMessage(mid: Int) {
        messageDao.delete(mid)
    }

    private suspend fun downgradeStagingMessage(uuid: String) {
        messageDao.failedStagingMessage(uuid)
    }

    override suspend fun fetchMessagesAtLeast(after: Long) = resultOf {
        messageService.getMessageAfter(after)
    }.saveIntoDBIfSuccess()


    private suspend fun Result<List<MessageDTO>>.saveIntoDBIfSuccess() {
        onSuccess { messages ->
            messages.forEach {
                messageDao.getById(it.id).also { old ->
                    if (old == null) {
                        messageDao.insert(it.toMessage())
                    }
                }
            }
        }
    }
}
