package com.linku.domain.service.impl

import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.service.ChatSocketService
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ChatSocketServiceImpl(
    private val client: HttpClient
) : ChatSocketService {
    private var socket: WebSocketSession? = null
    private var incoming: Flow<Frame>? = null

    override suspend fun initSession(): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
                url(ChatSocketService.EndPoints.DefaultSocket.url)
            }
            incoming = socket?.incoming?.receiveAsFlow()
            if (socket?.isActive == true) {
                Resource.Success(Unit)
            } else Resource.Failure("Couldn't establish a connection.")
        } catch (e: Exception) {
            e.printStackTrace()

            Resource.Failure(e.localizedMessage ?: "Unknown Error.")
        }
    }

    override suspend fun initSession(uid: Int): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
                url(ChatSocketService.EndPoints.UIDSocket(uid).url)
            }
            incoming = socket?.incoming?.receiveAsFlow()
            if (socket?.isActive == true) {
                Resource.Success(Unit)
            } else Resource.Failure("Couldn't establish a connection.")
        } catch (e: Exception) {
            e.printStackTrace()

            Resource.Failure(e.localizedMessage ?: "Unknown Error.")
        }
    }

    override fun observeMessages(): Flow<Message> {
        return try {
            incoming
                ?.filter { it is Frame.Text }
                ?.map {
                    val json = (it as Frame.Text).readText()
                    val message = Json.decodeFromString<Message>(json)
                    message
                }?.catch {
                    it.printStackTrace()
                } ?: flow { }
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override fun observeClose(): Flow<Frame.Close> {
        return try {
            incoming
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