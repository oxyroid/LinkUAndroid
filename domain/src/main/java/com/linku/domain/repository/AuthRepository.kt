package com.linku.domain.repository

import android.net.Uri
import androidx.annotation.Keep
import com.linku.domain.Resource
import com.linku.domain.Result
import kotlinx.coroutines.flow.Flow

@Keep
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
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