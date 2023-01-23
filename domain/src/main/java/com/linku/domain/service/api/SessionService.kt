package com.linku.domain.service.api

import com.linku.domain.BuildConfig
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow
import okhttp3.WebSocket

interface SessionService {
    fun initSession(uid: Int?): Flow<State>
    fun onMessage(): Flow<Message>

    sealed class EndPoints(val url: String) {
        /**
         * Each online user maps a Websocket url
         * If your ws_url is "ws://api.example.com/v1" and the userId is 4,
         * the url will be "ws://api.example.com/v1/4"
         */
        data class UIDSocket(val uid: Int) : EndPoints(BuildConfig.WS_URL + "/$uid")
    }

    sealed class State {
        object Connecting : State()
        data class Connected(val session: WebSocket) : State()
        object Closed : State()
        data class Failed(val reason: String?) : State()
    }
}