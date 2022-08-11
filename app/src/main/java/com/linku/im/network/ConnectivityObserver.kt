package com.linku.im.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe(): Flow<State>

    enum class State {
        Available, Unavailable, Losing, Lost
    }
}