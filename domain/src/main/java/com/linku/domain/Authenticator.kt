package com.linku.domain

import androidx.annotation.Keep
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

interface Authenticator {
    val currentUID: Int?
    val token: String?
    val observeCurrent: Flow<Int?>
    suspend fun update(uid: Int? = null, token: String? = null)

    @Keep
    @Serializable
    data class Token(
        val id: Int,
        val token: String
    )
}
