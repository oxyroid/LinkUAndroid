package com.linku.data.repository

import com.linku.domain.Resource
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.SessionRepository
import com.linku.domain.repository.SessionRepository.State.*
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.AuthService
import com.linku.domain.service.ConversationService
import com.linku.domain.service.NotificationService
import com.linku.domain.service.SessionService
import com.linku.domain.toResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionService: SessionService,
    private val authService: AuthService,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val conversationService: ConversationService,
    private val notificationService: NotificationService
) : SessionRepository {
    private val sessionState =
        MutableStateFlow<SessionRepository.State>(Default)
    private var session: WebSocket? = null

    override fun initSession(uid: Int?): Flow<Resource<Unit>> =
        sessionService.initSession(uid)
            .onEach { state ->
                when (state) {
                    SessionService.State.Connecting -> {
                        sessionState.tryEmit(Connecting)
                    }
                    is SessionService.State.Connected -> {
                        session = state.session
                        sessionState.tryEmit(Connected)
                    }
                    SessionService.State.Closed -> {
                        sessionState.tryEmit(Lost)
                    }
                    is SessionService.State.Failed -> {
                        sessionState.tryEmit(Failed(state.reason))
                    }
                }
            }
            .map { state ->
                when (state) {
                    SessionService.State.Connecting -> Resource.Loading
                    is SessionService.State.Connected -> Resource.Success(Unit)
                    SessionService.State.Closed -> Resource.Success(Unit)
                    is SessionService.State.Failed -> Resource.Failure(
                        state.reason ?: "Cannot initialize this session."
                    )
                }
            }
            .distinctUntilChanged()


    override fun subscribe(): Flow<Resource<Unit>> = channelFlow {
        trySend(Resource.Loading)
        if (sessionState.value != Connected) {
            trySend(Resource.Failure("You must subscribe it before the session has been initialized."))
        }
        sessionState.tryEmit(Subscribing)
        authService.subscribe()
            .toResult()
            .onSuccess {
                sessionState.tryEmit(Subscribed)
                trySend(Resource.Success(Unit))
                sessionService.onMessage()
                    .onEach {
                        messageDao.insert(it)
                        notificationService.onCollected(it)
                        val cid = it.cid
                        if (conversationDao.getById(it.id) == null) {
                            launch {
                                conversationService.getConversationById(cid)
                                    .toResult()
                                    .onSuccess { dto ->
                                        conversationDao.insert(dto.toConversation())
                                    }
                                    .onFailure {
                                        error("Cannot to save conversation, cid = $cid")
                                    }
                            }
                        }
                    }
                    .launchIn(this)
            }
            .onFailure {
                val message = it.message
                sessionState.tryEmit(Failed(message))
                trySend(Resource.Failure(message))
            }

    }

    override fun observerSessionState(): Flow<SessionRepository.State> = sessionState

    override suspend fun closeSession() {
        session?.close(1000, "")
    }
}