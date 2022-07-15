package com.linku.domain

import androidx.annotation.Keep
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * This is a local user manager.
 *
 */
object Auth {
    private const val AUTH_UID = "auth_uid"
    private const val AUTH_TOKEN = "auth_token"
    val currentUID: Int?
        get() = try {
            MMKV.defaultMMKV().decodeString(AUTH_UID)?.toInt()
        } catch (e: NumberFormatException) {
            null
        }


    val token: String?
        get() {
            if (currentUID == null) {
                val nullString: String? = null
                MMKV.defaultMMKV().encode(AUTH_TOKEN, nullString)
            }
            return MMKV.defaultMMKV().decodeString(AUTH_TOKEN)
        }

    private val _observeCurrent = MutableStateFlow(currentUID)
    val observeCurrent: Flow<Int?> = _observeCurrent

    suspend fun update(
        uid: Int? = null,
        token: String? = null
    ) {
        Json.encodeToString(uid).also {
            MMKV.defaultMMKV().encode(AUTH_UID, it)
            MMKV.defaultMMKV().encode(AUTH_TOKEN, token)
            _observeCurrent.emit(uid)
        }
    }

    @Keep
    @Serializable
    data class Token(
        val id: Int,
        val token: String
    )
}