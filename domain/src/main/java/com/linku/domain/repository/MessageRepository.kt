package com.linku.domain.repository

import android.net.Uri
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import kotlinx.coroutines.flow.Flow
import java.util.*

interface MessageRepository {
    fun getMessageById(mid: Int): Flow<Resource<Message>>
    fun initSession(uid: Int?): Flow<Resource<Unit>>
    fun incoming(): Flow<List<Message>>
    fun incoming(cid: Int): Flow<List<Message>>
    suspend fun closeSession()
    suspend fun sendTextMessage(
        cid: Int,
        text: String
    ): Flow<Resource<Unit>>

    fun sendImageMessage(
        cid: Int,
        uri: Uri
    ): Flow<Resource<Unit>>

    fun sendGraphicsMessage(
        cid: Int,
        text: String,
        uri: Uri
    ): Flow<Resource<Unit>>

    suspend fun resendStagingMessage(uuid: String)

    suspend fun fetchUnreadMessages()

    sealed class StagingMessage {
        data class Text(
            val cid: Int,
            val uid: Int,
            val text: String
        ) : StagingMessage()

        data class Image(
            val cid: Int,
            val uid: Int,
            val uri: Uri
        ) : StagingMessage()

        data class Graphics(
            val cid: Int,
            val uid: Int,
            val text: String,
            val uri: Uri
        ) : StagingMessage()

        val uuid: String by lazy { UUID.randomUUID().toString() }
    }
}