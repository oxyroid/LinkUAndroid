package com.wzk.domain.service

import com.wzk.domain.entity.Message
import com.wzk.domain.entity.TextMessage
import com.wzk.wrapper.Resource
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
    override suspend fun initSession(uid: Int, cid: Int): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
//                url("${ChatSocketService.EndPoints.ChatSocket.url}/$cid?uid=$uid")
                url("${ChatSocketService.EndPoints.ChatSocket.url}")
            }
            if (socket?.isActive == true) {
                Resource.Success(Unit)
            } else Resource.Failure(1, "Couldn't establish a connection.")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(1, e.localizedMessage ?: "Unknown Error.")
        }
    }

    override suspend fun sendMessage(message: Message) {
        try {
            socket?.send(Frame.Text(Json.encodeToString(message)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeMessages(cid: Int): Flow<Message> {
        return try {
            socket?.incoming
                ?.receiveAsFlow()
                ?.filter { true }
                ?.map {
                    val json = (it as Frame.Text).readText()
                    // FIXME
//                    val message = Json.decodeFromString<Message>(json)
                    val message = TextMessage(cid = cid, uid = 1, text = json)
                    message
                } ?: flow { }
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override suspend fun closeSession() {
        socket?.close()
    }
}