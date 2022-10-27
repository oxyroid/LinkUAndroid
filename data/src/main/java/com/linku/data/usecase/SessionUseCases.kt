package com.linku.data.usecase

import com.linku.domain.repository.SessionRepository
import com.linku.domain.wrapper.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class SessionUseCases @Inject constructor(
    val init: InitSessionUseCase,
    val subscribeRemote: SubscribeUseCase,
    val state: ObserverSessionStateUseCase,
    val close: CloseSessionUseCase
)

data class InitSessionUseCase @Inject constructor(
    val repository: SessionRepository
) {
    operator fun invoke(uid: Int?): Flow<Resource<Unit>> {
        return repository.initSession(uid)
    }
}

data class SubscribeUseCase @Inject constructor(
    val repository: SessionRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> {
        return repository.subscribeRemote()
    }
}

data class ObserverSessionStateUseCase @Inject constructor(
    val repository: SessionRepository
) {
    operator fun invoke(): Flow<SessionRepository.State> {
        return repository.observerSessionState()
    }
}

data class CloseSessionUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    suspend operator fun invoke() {
        repository.closeSession()
    }
}
