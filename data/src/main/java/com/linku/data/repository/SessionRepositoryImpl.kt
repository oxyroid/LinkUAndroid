package com.linku.data.repository

import com.linku.data.R
import com.linku.data.usecase.ApplicationUseCases
import com.linku.domain.Resource
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.SessionRepository
import com.linku.domain.repository.SessionRepository.State.*
import com.linku.domain.resultOf
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.service.AuthService
import com.linku.domain.service.ConversationService
import com.linku.domain.service.NotificationService
import com.linku.domain.service.SessionService
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
    private val notificationService: NotificationService,
    private val applications: ApplicationUseCases
) : SessionRepository {
    private var session: WebSocket? = null
    private val sessionState = MutableStateFlow<SessionRepository.State>(Default)

    override fun initSession(uid: Int?): Flow<Resource<Unit>> {
        return sessionService.initSession(uid)
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
                    is SessionService.State.Failed -> {
                        val msg = applications.getString(R.string.error_session_initialize)
                        Resource.Failure(
                            state.reason ?: msg
                        )
                    }
                }
            }
            .distinctUntilChanged()
    }

    override fun subscribeRemote(): Flow<Resource<Unit>> = channelFlow {
        trySend(Resource.Loading)
        if (sessionState.value != Connected) {
            val msg = applications.getString(R.string.error_session_subscribe)
            trySend(Resource.Failure(msg))
        }
        sessionState.tryEmit(Subscribing)
        resultOf {
            authService.subscribe()
        }
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
                                resultOf {
                                    conversationService.getConversationById(cid)
                                }
                                    .onSuccess { dto ->
                                        conversationDao.insert(dto.toConversation())
                                    }
                                    .onFailure {
                                        val msg = applications.getString(
                                            R.string.error_save_conversation,
                                            cid
                                        )
                                        error(msg)
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
