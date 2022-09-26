package com.linku.data.service

import com.linku.domain.entity.Message
import com.linku.domain.entity.MessageDTO
import com.linku.domain.service.SessionService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import javax.inject.Inject

class SessionServiceImpl @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) : SessionService {
    private val incoming = MutableSharedFlow<Message>()
    override fun initSession(uid: Int?): Flow<SessionService.State> = callbackFlow {
        trySend(SessionService.State.Connecting)
        if (uid == null) {
            trySend(SessionService.State.Failed("User is not exist."))
            return@callbackFlow
        }
        val request = Request.Builder()
            .url(SessionService.EndPoints.UIDSocket(uid).url)
            .build()
        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                trySend(SessionService.State.Connected(webSocket))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                launch {
                    try {
                        json.decodeFromString<SocketPackage<MessageDTO>>(text).data.toMessage()
                    } catch (e: Exception) {
                        null
                    }?.also {
                        incoming.emit(it)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                launch {
                    trySend(SessionService.State.Closed)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                trySend(SessionService.State.Failed(t.message))
            }
        })
        awaitClose {
            socket.close(1000, null)
        }
    }

    override fun onMessage(): Flow<Message> = incoming
}