package com.linku.data.authenticator

import com.linku.data.usecase.Configurations
import com.linku.domain.auth.Authenticator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class PreferenceAuthenticator(
    private val configurations: Configurations
) : Authenticator {
    override val currentUID: Int?
        get() = try {
            val uid = configurations.currentUID
            check(uid >= 0)
            uid
        } catch (e: IllegalStateException) {
            null
        }

    override val token: String?
        get() {
            if (currentUID == null) {
                configurations.token = null
            }
            return configurations.token
        }

    private val _observeCurrent = MutableStateFlow(currentUID)
    override val observeCurrent: Flow<Int?> = _observeCurrent

    override suspend fun update(
        uid: Int?,
        token: String?
    ) {
        configurations.currentUID = uid ?: -1
        configurations.token = token
        _observeCurrent.emit(uid)
    }

}
