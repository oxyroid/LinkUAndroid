package com.linku.domain.repository

import androidx.annotation.Keep
import com.linku.domain.Result

@Keep
interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ): Result<Unit>

    suspend fun verifyEmail(code: String): Result<Unit>

    suspend fun resendEmail(): Result<Unit>

    suspend fun clearLocal()
}