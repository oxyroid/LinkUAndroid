package com.linku.domain.repository

import android.net.Uri
import com.linku.core.wrapper.Resource
import com.linku.domain.Strategy
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessageById(
        mid: Int,
        strategy: Strategy
    ): Message?

    fun incoming(): Flow<List<Message>>
    fun incoming(cid: Int): Flow<List<Message>>

    fun observeLatestMessage(cid: Int): Flow<Message>

    suspend fun sendTextMessage(
        cid: Int,
        text: String,
        reply: Int?
    ): Flow<Resource<Unit>>

    fun sendImageMessage(
        cid: Int,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>>

    fun sendGraphicsMessage(
        cid: Int,
        text: String,
        uri: Uri,
        reply: Int?
    ): Flow<Resource<Unit>>

    suspend fun cancelMessage(mid: Int)

    suspend fun resendMessage(mid: Int): Flow<Resource<Unit>>

    suspend fun fetchMessagesAtLeast(after: Long)

}
