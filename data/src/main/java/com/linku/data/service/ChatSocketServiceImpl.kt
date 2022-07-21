package com.linku.data.service

import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.entity.MessageDTO
import com.linku.domain.service.ChatSocketService
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ChatSocketServiceImpl(
    private val client: HttpClient
) : ChatSocketService {
    private var socket: WebSocketSession? = null

    override suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
                url(ChatSocketService.EndPoints.UIDSocket(uid).url)
                contentType(ContentType.Application.Json)
            }
            if (socket?.isActive == true) {
                incoming = socket?.incoming?.consumeAsFlow()?.stateIn(scope)
                Resource.Success(Unit)
            } else Resource.Failure("Couldn't establish a connection.")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.localizedMessage ?: "Unknown Error.")
        }
    }

    private var incoming: Flow<Frame>? = null

    override fun incoming(): Flow<Message> {
        return try {
            incoming
                ?.filter { it is Frame.Text }
                ?.mapNotNull {
                    val text = (it as Frame.Text).readText()
                    val json = Json { ignoreUnknownKeys = true }
                    try {
                        json.decodeFromString<Socket<MessageDTO>>(text).data.toMessage()
                    } catch (e: Exception) {
                        debug { Log.e(TAG, "Json Error: ", e) }
                        null
                    }
                } ?: flow { }
        } catch (e: Exception) {
            debug { Log.e(TAG, "Incoming Receive Error: ", e) }
            flow { }
        }
    }

    override suspend fun onClosed(handler: suspend () -> Unit) {
        try {
            incoming
                ?.filter { it is Frame.Close }
                ?.onEach { handler() }
                ?.collect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun closeSession() {
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
