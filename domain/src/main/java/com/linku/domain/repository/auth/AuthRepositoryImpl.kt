package com.linku.domain.repository.auth

import com.linku.domain.Auth
import com.linku.domain.Result
import com.linku.domain.entity.User
import com.linku.domain.room.dao.UserDao
import com.linku.domain.sandbox
import com.linku.domain.service.AuthService

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userDao: UserDao
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> =
        sandbox {
            authService.login(email, password)
                .map { it.toUser() }
                .handle {
                    userDao.insert(it)
                    // FIXME: USE TOKEN INSTEAD
                    Auth.update(it)
                }
        }

    override suspend fun register(
        email: String,
        password: String,
        nickName: String,
        realName: String?
    ): Result<Unit> = sandbox {
        authService.register(email, password, nickName, realName)
    }

    override suspend fun verifyEmail(code: Int) = sandbox {
        authService.verifyEmail(code)
    }

    override suspend fun resendEmail() = sandbox {
        authService.resendEmail()
    }
}