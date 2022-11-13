package com.linku.data.usecase

import android.net.Uri
import com.linku.core.wrapper.Resource
import com.linku.core.wrapper.resultOf
import com.linku.domain.Strategy
import com.linku.domain.entity.Message
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.MessageRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.api.ConversationService
import com.linku.domain.service.api.MessageService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MessageUseCases @Inject constructor(
    val observeAllMessages: ObserveAllMessagesUseCase,
    val observeMessages: ObserveMessagesUseCase,
    val observeLatestMessage: ObserveLatestMessagesUseCase,
    val textMessage: TextMessageUseCase,
    val imageMessage: ImageMessageUseCase,
    val graphicsMessage: GraphicsMessageUseCase,
    val cancelMessage: CancelMessageUseCase,
    val resendMessage: ResendMessageUseCase,
    val getMessage: GetMessageUseCase,
    val syncingMessages: SyncingUseCase,
    val fetchMessagesAtLeastUseCase: FetchMessagesAtLeastUseCase,
    val findMessagesByType: FindMessageByTypeUseCase,
    val contactRequest: ContactRequestUseCase,
    val queryMessages: QueryMessagesUseCase
)

data class QueryMessagesUseCase @Inject constructor(
    private val messageDao: MessageDao
) {
    suspend operator fun invoke(
        key: String
    ): List<Message> {
        return messageDao.query(key)
    }
}

data class ContactRequestUseCase @Inject constructor(
    private val messageService: MessageService
) {
    operator fun invoke(
        uid: Int,
        content: String = ""
    ): Flow<Resource<Unit>> = channelFlow {
        trySend(Resource.Loading)
        launch {
            resultOf { messageService.request(uid, content) }
                .onSuccess {
                    trySend(Resource.Success(Unit))
                }
                .onFailure {
                    trySend(Resource.Failure(it.message))
                }
        }
    }
}

data class FindMessageByTypeUseCase @Inject constructor(
    private val messageDao: MessageDao
) {
    @Suppress("UNCHECKED_CAST")
    suspend operator fun <E: Message> invoke(type: Message.Type): List<E> {
        return messageDao.findByType(type).map { it.toReadable() as E }
    }
}

data class CancelMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(mid: Int) {
        repository.cancelMessage(mid)
    }
}

data class ResendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(mid: Int): Flow<Resource<Unit>> {
        return repository.resendMessage(mid)
    }
}

data class SyncingUseCase @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val messageService: MessageService,
    private val conversationService: ConversationService,
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        resultOf { messageService.getMessageAfter(getLatestLocalMessageTime()) }
            .onSuccess { result ->
                result.sortedBy { it.cid }.forEach { message ->
                    if (messageDao.getById(message.id) == null) {
                        messageDao.insert(message.toMessage())
                    }
                    if (conversationDao.getById(message.cid) == null) {
                        launch {
                            resultOf {
                                conversationService.getConversationById(message.cid)
                            }.onSuccess {
                                conversationDao.insert(it.toConversation())
                            }
                        }
                    }
                }
            }
    }

    private suspend fun getLatestLocalMessageTime(): Long {
        return messageDao.getLatestMessage()?.timestamp ?: 0
    }
}

data class ObserveLatestMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(cid: Int): Flow<Message> = repository.observeLatestMessage(cid)
}

data class FetchMessagesAtLeastUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(
        after: Long
    ) = repository.fetchMessagesAtLeast(after)
}


data class GetMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(
        mid: Int,
        strategy: Strategy
    ): Message? {
        return repository.getMessageById(mid, strategy)
    }
}

data class TextMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(
        cid: Int,
        text: String,
        reply: Int? = null
    ): Flow<Resource<Unit>> = repository.sendTextMessage(cid, text, reply)
}

data class ImageMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(
        cid: Int,
        uri: Uri,
        reply: Int? = null
    ): Flow<Resource<Unit>> = messageRepository.sendImageMessage(cid, uri, reply)
}

data class GraphicsMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    operator fun invoke(
        cid: Int,
        text: String,
        uri: Uri,
        reply: Int? = null
    ): Flow<Resource<Unit>> = messageRepository.sendGraphicsMessage(cid, text, uri, reply)
}

data class ObserveAllMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(): Flow<List<Message>> {
        return repository.incoming()
    }
}

data class ObserveMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(cid: Int): Flow<List<Message>> {
        return repository.incoming(cid)
    }
}

