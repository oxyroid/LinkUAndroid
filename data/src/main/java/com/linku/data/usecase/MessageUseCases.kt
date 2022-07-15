package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class MessageUseCases @Inject constructor(
    val textMessageUseCase: TextMessageUseCase,
    val initSessionUseCase: InitSessionUseCase,
    val observeMessagesUseCase: ObserveMessagesUseCase,
    val observeMessagesByCIDUseCase: ObserveMessagesByCidUseCase,
    val closeSessionUseCase: CloseSessionUseCase
)

data class TextMessageUseCase(
    val repository: MessageRepository
) {
    suspend operator fun invoke(
        cid: Int, content: String
    ): Flow<Resource<Unit>> = repository.sendTextMessage(cid, content)
}

data class InitSessionUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(
        uid: Int,
        scope: CoroutineScope
    ): Resource<Unit> = repository.initSession(uid, scope)
}

data class ObserveMessagesUseCase(
    private val repository: MessageRepository
) {
    operator fun invoke(): Flow<List<Message>> {
        return repository.incoming()
    }
}

data class ObserveMessagesByCidUseCase(
    private val repository: MessageRepository
) {
    operator fun invoke(cid: Int): Flow<List<Message>> {
        return repository.incoming(cid)
    }
}

data class CloseSessionUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke() {
        repository.closeSession()
    }
}
