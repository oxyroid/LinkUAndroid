package com.linku.data.repository

import com.linku.domain.Auth
import com.linku.domain.Result
import com.linku.domain.repository.AuthRepository
import com.linku.domain.sandbox
import com.linku.domain.service.AuthService

class AuthRepositoryImpl(
    private val authService: AuthService,
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<Unit> =
        sandbox {
            authService.signIn(email, password)
                .handle {
                    Auth.update(uid = it.id, token = it.token)
                }.map {}
        }

    override suspend fun signUp(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ): Result<Unit> = sandbox {
        authService.signUp(email, password, nickName, realName)
    }

    override suspend fun verifyEmail(code: String) = sandbox {
        authService.verifyEmailCode(code)
    }

    override suspend fun resendEmail() = sandbox {
        authService.verifyEmail()
    }
}