package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.repository.MessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class MessageUseCases @Inject constructor(
    val textMessageUseCase: TextMessageUseCase,
    val dispatcherUseCase: DispatcherUseCase,
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
    ): Resource<Unit> = repository.sendTextMessage(cid, content).toUnitResource()
}

data class DispatcherUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(): Resource<Unit> = repository.subscribe().toUnitResource()
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
    operator fun invoke(
        scope: CoroutineScope
    ): Flow<List<Message>> {
        repository.persistence(scope)
        return repository.incoming()
    }
}

data class ObserveMessagesByCidUseCase(
    private val repository: MessageRepository
) {
    operator fun invoke(scope: CoroutineScope, cid: Int): Flow<List<Message>> {
        repository.persistence(scope)
        return repository.incoming()
            .map {
                it.filter { message -> message.cid == cid }.toList()
            }
    }
}

data class CloseSessionUseCase(
    private val repository: MessageRepository
) {
    suspend operator fun invoke() {
        repository.closeSession()
    }
}
