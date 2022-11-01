@file:Suppress("unused")

package com.linku.data.authenticator

import android.content.Context
import com.linku.core.fs.impl.readFs
import com.linku.core.fs.impl.writeFs
import com.linku.domain.auth.Authenticator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CryptoAuthenticator(
    private val context: Context
) : Authenticator {
    override val currentUID: Int?
        get() = context.readFs.decrypt().split('&').firstOrNull().let {
            if (it.isNullOrEmpty()) null
            else it
        }?.toInt()
    override val token: String?
        get() = context.readFs.decrypt().split('&').getOrNull(1).let {
            if (it.isNullOrEmpty()) null
            else it
        }
    private val _observeCurrent = MutableStateFlow(currentUID)
    override val observeCurrent: Flow<Int?>
        get() = _observeCurrent

    override suspend fun update(uid: Int?, token: String?) {
        if (uid == null || token == null) {
            context.writeFs.encrypt("&")
            _observeCurrent.emit(null)
        } else {
            val s = "$uid&$token"
            context.writeFs.encrypt(s)
            _observeCurrent.emit(uid)
        }
    }
}
