package com.linku.data.service.api

import androidx.annotation.Keep
import com.linku.core.extension.json
import com.linku.data.R
import com.linku.data.usecase.ApplicationUseCases
import com.linku.domain.entity.Message
import com.linku.domain.entity.MessageDTO
import com.linku.domain.service.api.SessionService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import javax.inject.Inject

class SessionServiceImpl @Inject constructor(
    private val client: OkHttpClient,
    private val applications: ApplicationUseCases
) : SessionService {
    private val incoming = MutableSharedFlow<Message>()
    override fun initSession(uid: Int?): Flow<SessionService.State> = callbackFlow {
        trySend(SessionService.State.Connecting)
        if (uid == null) {
            val msg = applications.getString(R.string.error_user_not_exist)
            trySend(SessionService.State.Failed(msg))
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
                    val message = runCatching {
                        val socketPackage = json.decodeFromString<Package<MessageDTO>>(text)
                        socketPackage.data.toMessage()
                    }
                        .getOrNull()
                    message?.also { incoming.emit(it) }
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

@Keep
@Serializable
internal data class Package<T>(
    val data: T,
    val type: String
)
