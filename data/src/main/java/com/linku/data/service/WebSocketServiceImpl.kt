package com.linku.data.service

import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.entity.MessageDTO
import com.linku.domain.service.ChatService
import com.linku.domain.service.WebSocketService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString

class WebSocketServiceImpl(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
    private val chatService: ChatService
) : WebSocketService {
    private var webSocket: WebSocket? = null
    private var messageFlow = MutableSharedFlow<Message>()
    private var onClosed: (suspend () -> Unit)? = null

    override fun initSession(uid: Int?): Flow<Resource<Unit>> = callbackFlow {
        trySend(Resource.Loading)
        if (uid == null) {
            trySend(Resource.Failure("User is not exist."))
            return@callbackFlow
        }
        val request = Request.Builder()
            .url(WebSocketService.EndPoints.UIDSocket(uid).url)
            .build()
        okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                this@WebSocketServiceImpl.webSocket = webSocket
                trySend(Resource.Success(Unit))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                launch {
                    try {
                        json.decodeFromString<SocketPackage<MessageDTO>>(text).data.toMessage()
                    } catch (e: Exception) {
                        debug { Log.e(TAG, "onMessage: ", e) }
                        null
                    }?.let { messageFlow.emit(it) }

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
                debug {
                    Log.e(TAG, "Websocket closed, code = $code, reason = $reason")
                }
                launch {
                    onClosed?.invoke()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                debug {
                    Log.e(TAG, "Websocket failed", t)
                }
                trySend(Resource.Failure(t.message ?: ""))
            }
        })
        awaitClose {
            webSocket?.close(1000, null)
        }
    }

    override fun incoming(): Flow<Message> = messageFlow

    override suspend fun closeSession() {
        webSocket?.close(1000, null)
    }

    override suspend fun onClosed(handler: suspend () -> Unit) {
        onClosed = handler
    }
}