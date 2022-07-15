package com.linku.data.repository

import com.linku.domain.Auth
import com.linku.domain.Result
import com.linku.domain.repository.AuthRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.sandbox
import com.linku.domain.service.AuthService

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userDao: UserDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<Unit> =
        sandbox {
            authService.signIn(email, password)
                .handle {
                    Auth.update(uid = it.id, token = it.token)
                }
                .map {}
        }

    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        realName: String?
    ): Result<Unit> = sandbox {
        authService.signUp(email, password, name, realName)
    }

    override suspend fun verifyEmail(code: String) = sandbox {
        authService.verifyEmailCode(code)
    }

    override suspend fun resendEmail() = sandbox {
        authService.verifyEmail()
    }

    override suspend fun signOut() {
        userDao.clear()
        conversationDao.clear()
        messageDao.clear()
    }
}