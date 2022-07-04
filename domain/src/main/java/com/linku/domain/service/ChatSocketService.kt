package com.linku.domain.service

import com.linku.domain.BuildConfig
import com.linku.domain.Resource
import com.linku.domain.common.buildUrl
import com.linku.domain.entity.Message
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow

interface ChatSocketService {
    @Deprecated("")
    suspend fun initSession(): Resource<Unit>
    suspend fun initSession(uid: Int): Resource<Unit>
    fun observeMessages(): Flow<Message>
    fun observeClose(): Flow<Frame.Close>
    suspend fun closeSession()

    companion object {
        private const val BASE_URL = BuildConfig.WS_URL
    }

    sealed class EndPoints(val url: String) {
        object DefaultSocket : EndPoints(BASE_URL)
        data class UIDSocket(val uid: Int) : EndPoints(
            buildUrl(BASE_URL) {
                path(uid)
            }
        )
    }
}