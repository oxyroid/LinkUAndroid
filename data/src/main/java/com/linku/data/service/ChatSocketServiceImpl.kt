package com.linku.data.service

import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.service.ChatSocketService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ChatSocketServiceImpl(
    private val client: HttpClient
) : ChatSocketService {
    private lateinit var socket: WebSocketSession

    override suspend fun initSession(): Resource<Unit> {
        return try {
            socket = client.webSocketSession {
                url(ChatSocketService.EndPoints.DefaultSocket.url)
            }
            if (socket.isActive) {
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
            if (socket.isActive) {
                Resource.Success(Unit)
            } else Resource.Failure("Couldn't establish a connection.")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.localizedMessage ?: "Unknown Error.")
        }
    }

    override fun observeMessages(): Flow<Message> {
        return try {
            socket.incoming.receiveAsFlow()
                .filter { it is Frame.Text }
                .map {
                    val json = (it as Frame.Text).readText()
                    try {
                        Json.decodeFromString<Message>(json)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                .filterNotNull()
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override fun observeClose(): Flow<Frame.Close> {
        return try {
            socket.incoming.receiveAsFlow()
                .filter { it is Frame.Close }
                .map { it as Frame.Close }
        } catch (e: Exception) {
            e.printStackTrace()
            flow { }
        }
    }

    override suspend fun closeSession() {
        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}