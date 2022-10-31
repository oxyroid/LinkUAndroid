package com.linku.domain.repository

import android.net.Uri
import com.linku.domain.Strategy
import com.linku.domain.bean.ui.MessageUI
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun getMessageById(
        mid: Int,
        strategy: Strategy
    ): Message?
    fun incoming(): Flow<List<Message>>
    fun incoming(cid: Int): Flow<List<Message>>
    fun observeLatestMessageVOs(
        cid: Int,
        attachPrevious: Boolean
    ): Flow<List<MessageUI>>

    fun observeLatestMessage(cid: Int): Flow<Message>

    suspend fun sendTextMessage(
        cid: Int,
        text: String,
        reply: Int?
    ): Flow<com.linku.core.wrapper.Resource<Unit>>

    fun sendImageMessage(
        cid: Int,
        uri: Uri,
        reply: Int?
    ): Flow<com.linku.core.wrapper.Resource<Unit>>

    fun sendGraphicsMessage(
        cid: Int,
        text: String,
        uri: Uri,
        reply: Int?
    ): Flow<com.linku.core.wrapper.Resource<Unit>>

    suspend fun cancelMessage(mid: Int)

    suspend fun resendMessage(mid: Int): Flow<com.linku.core.wrapper.Resource<Unit>>

    suspend fun fetchUnreadMessages()

    suspend fun fetchMessagesAtLeast(after: Long)

}
