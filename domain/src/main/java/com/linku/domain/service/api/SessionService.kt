package com.linku.domain.service.api

import com.linku.domain.BuildConfig
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow
import okhttp3.WebSocket

interface SessionService {
    fun initSession(uid: Int?): Flow<State>
    fun onMessage(): Flow<Message>

    sealed class EndPoints(val url: String) {
        data class UIDSocket(val uid: Int) : EndPoints(BuildConfig.WS_URL + "/$uid")
    }

    sealed class State {
        object Connecting : State()
        data class Connected(val session: WebSocket) : State()
        object Closed : State()
        data class Failed(val reason: String?) : State()
    }
}