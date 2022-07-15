package com.linku.domain.repository

import com.linku.domain.Resource
import com.linku.domain.Result
import com.linku.domain.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit>
    fun incoming(): Flow<List<Message>>
    fun incoming(cid: Int): Flow<List<Message>>
    suspend fun closeSession()
    suspend fun sendTextMessage(cid: Int, content: String): Flow<Resource<Unit>>
}