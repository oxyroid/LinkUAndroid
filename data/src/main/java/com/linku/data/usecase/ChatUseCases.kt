package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.emitResource
import com.linku.domain.repository.ChatRepository
import com.linku.domain.resourceFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class ChatUseCases @Inject constructor(
    val sendTextMessageUseCase: SendTextMessageUseCase,
    val subscribeUseCase: SubscribeUseCase
)

data class SendTextMessageUseCase(
    val repository: ChatRepository
) {
    operator fun invoke(
        cid: Int,
        content: String
    ): Flow<Resource<Unit>> = resourceFlow {
        repository.sendTextMessage(cid, content)
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}

data class SubscribeUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = resourceFlow {
        repository.subscribe()
            .handleUnit(::emitResource)
            .catch(::emitResource)
    }
}