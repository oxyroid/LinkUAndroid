package com.linku.domain.repository

import androidx.annotation.Keep
import com.linku.domain.Result
import com.linku.domain.entity.User

@Keep
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ): Result<Unit>

    suspend fun verifyEmail(code: Int): Result<Unit>

    suspend fun resendEmail(): Result<Unit>
}