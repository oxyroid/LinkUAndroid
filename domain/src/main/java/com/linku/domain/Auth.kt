package com.linku.domain

import com.linku.domain.entity.User
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * This is a local user manager.
 *
 */
object Auth {
    private const val AUTH_USER = "auth_user"
    val current: User?
        get() = MMKV.defaultMMKV().decodeString(AUTH_USER)?.let(Json::decodeFromString)

    private val _observeCurrent = MutableStateFlow(current)
    val observeCurrent: Flow<User?> = _observeCurrent

    suspend fun update(user: User?) {
        Json.encodeToString(user).also {
            MMKV.defaultMMKV().encode(AUTH_USER, it)
            _observeCurrent.emit(user)
        }
    }
}