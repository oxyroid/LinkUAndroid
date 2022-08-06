package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.BuildConfig
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Keep
interface ChatSocketService {
    suspend fun initSession(uid: Int): Flow<Resource<Unit>>
    fun incoming(): Flow<Message>
    suspend fun closeSession()
    suspend fun onClosed(handler: suspend () -> Unit)

    sealed class EndPoints(val url: String) {
        data class UIDSocket(val uid: Int) : EndPoints(BuildConfig.WS_URL + "/$uid")
    }
}