package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.BuildConfig
import com.linku.domain.Resource
import com.linku.domain.common.buildUrl
import com.linku.domain.entity.Message
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow

@Keep
interface ChatSocketService {
    suspend fun initSession(): Resource<Unit>
    suspend fun initSession(uid: Int): Resource<Unit>
    fun observeMessages(): Flow<Message>
    fun observeClose(): Flow<Frame.Close>
    suspend fun closeSession()

    sealed class EndPoints(val url: String) {
        object DefaultSocket : EndPoints(BuildConfig.WS_URL)
        data class UIDSocket(val uid: Int) : EndPoints(
            buildUrl(BuildConfig.WS_URL) {
                path(uid)
            }
        )
    }
}