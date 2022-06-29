package com.wzk.domain

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.flow.channelFlow
import okhttp3.*

sealed class WsPack {
    data class Open(
        val webSocket: WebSocket,
        val response: Response
    ) : WsPack()

    data class Closed(
        val webSocket: WebSocket,
        val code: Int,
        val reason: String
    ) : WsPack()

    data class Message(
        val webSocket: WebSocket,
        val text: String
    ) : WsPack()

    data class Failure(
        val webSocket: WebSocket,
        val t: Throwable,
        val response: Response?
    ) : WsPack()
}

fun wsFlow(ws: String) = channelFlow {
    val request = Request.Builder().url(ws).build()
    val socket = OkHttpClient().newWebSocket(request, object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            trySend(WsPack.Open(webSocket, response)).onFailure(::println)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            trySend(WsPack.Closed(webSocket, code, reason)).onFailure(::println)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            trySend(WsPack.Message(webSocket, text)).onFailure(::println)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            trySend(WsPack.Failure(webSocket, t, response)).onFailure(::println)
        }
    })
    awaitClose { socket.cancel() }
}