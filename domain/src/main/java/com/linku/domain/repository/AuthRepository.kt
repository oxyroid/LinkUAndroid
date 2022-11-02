package com.linku.domain.repository

import android.net.Uri
import com.linku.core.wrapper.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signIn(
        email: String,
        password: String
    ): Flow<Resource<Unit>>

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        realName: String?
    ): Result<Unit>

    suspend fun signOut()

    suspend fun verifyEmailCode(code: String): Result<Unit>

    suspend fun verifyEmail(): Result<Unit>

    fun uploadAvatar(uri: Uri): Flow<Resource<Unit>>

}
