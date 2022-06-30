package com.linku.domain.service.impl

import com.linku.domain.entity.Message
import com.linku.domain.entity.TextMessage
import com.linku.domain.service.ChatSocketService
import com.linku.wrapper.Resource
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatSocketServiceImpl(
    private val client: HttpClient
) : ChatSocketService {
    private var socket: WebSocketSession? = null
    private var receivedFlow: Flow<Frame>? = null

    override suspend fun initSession(uid: Int, cid: Int): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
                // url("${ChatSocketService.EndPoints.ChatSocket.url}/$cid?uid=$uid")
                url(ChatSocketService.EndPoints.TestSocket.url)
            }
            receivedFlow = socket?.incoming?.receiveAsFlow()
            if (socket?.isActive == true) {
                Resource.Success(Unit)
            } else Resource.Failure("Couldn't establish a connection.")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.localizedMessage ?: "Unknown Error.")
        }
    }

    override suspend fun sendMessage(message: Message) {
        try {
            socket?.send(Frame.Text(Json.encodeToString(message)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeMessages(): Flow<Message> {
        return try {
            receivedFlow
                ?.filter { it is Frame.Text }
                ?.map {
                    val json = (it as Frame.Text).readText()
                    // FIXME
                    // val message = Json.decodeFromString<Message>(json)
                    val message = TextMessage(cid = 1, uid = 1, text = json)
                    message
                } ?: flow { }
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override fun observeClose(): Flow<Frame.Close> {
        return try {
            receivedFlow
                ?.filter { it is Frame.Close }
                ?.map { it as Frame.Close }
                ?: flow { }
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override suspend fun closeSession() {
        socket?.close()
    }
}