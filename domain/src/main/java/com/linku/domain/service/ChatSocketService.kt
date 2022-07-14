package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.BuildConfig
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@Keep
interface ChatSocketService {
    suspend fun initSession(uid: Int, scope: CoroutineScope): Resource<Unit>
    fun incoming(): Flow<Message>
    fun observeClose(): Flow<Frame.Close>
    suspend fun closeSession()

    sealed class EndPoints(val url: String) {
        data class UIDSocket(val uid: Int) : EndPoints(BuildConfig.WS_URL + "/$uid")
    }
}