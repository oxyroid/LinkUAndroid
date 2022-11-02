package com.linku.domain.repository

import com.linku.core.wrapper.Resource
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun initSession(uid: Int?): Flow<Resource<Unit>>
    fun subscribeRemote(): Flow<Resource<Unit>>
    suspend fun closeSession()
    fun observerSessionState(): Flow<State>

    sealed class State {
        object Default : State()
        object Connecting : State()
        object Connected : State()
        object Subscribing : State()
        object Subscribed : State()
        object Lost : State()
        data class Failed(val reason: String?) : State()
    }
}
