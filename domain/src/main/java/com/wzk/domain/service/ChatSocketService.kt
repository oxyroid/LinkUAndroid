package com.wzk.domain.service

import com.wzk.domain.entity.Message
import com.wzk.wrapper.Resource
import kotlinx.coroutines.flow.Flow

interface ChatSocketService {
    suspend fun initSession(uid: Int, cid: Int): Resource<Unit>
    suspend fun sendMessage(message: Message)
    fun observeMessages(cid: Int): Flow<Message>
    suspend fun closeSession()

    companion object {
        const val BASE_URL = "wss://im.rexue.work/ws"
    }

    sealed class EndPoints(val url: String) {
        object ChatSocket : EndPoints("$BASE_URL")
    }
}