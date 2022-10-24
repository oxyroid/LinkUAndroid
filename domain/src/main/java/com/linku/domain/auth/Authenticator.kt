package com.linku.domain.auth

import kotlinx.coroutines.flow.Flow

interface Authenticator {
    val currentUID: Int?
    val token: String?
    val observeCurrent: Flow<Int?>
    suspend fun update(uid: Int? = null, token: String? = null)
}
