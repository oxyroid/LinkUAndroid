package com.linku.domain.repository

import com.linku.domain.Resource
import com.linku.domain.Result
import com.linku.domain.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit>
    fun persistence(scope: CoroutineScope)
    fun incoming(): Flow<List<Message>>
    suspend fun closeSession()
    suspend fun sendTextMessage(cid: Int, content: String): Result<Unit>
    suspend fun subscribe(): Result<Unit>
}