package com.linku.domain.service

import com.linku.domain.entity.Message
import com.linku.wrapper.Resource
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow

interface ChatSocketService {
    suspend fun initSession(uid: Int, cid: Int): Resource<Unit>
    suspend fun sendMessage(message: Message)
    fun observeMessages(): Flow<Message>
    fun observeClose(): Flow<Frame.Close>
    suspend fun closeSession()

    companion object {
        private const val BASE_URL = "wss://im.rexue.work/ws"
    }

    sealed class EndPoints(val url: String) {
        data class ChatSocket(val cid: Int) : EndPoints("$BASE_URL/$cid")
        object TestSocket : EndPoints(BASE_URL)
    }
}