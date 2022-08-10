package com.linku.data.repository

import com.linku.domain.Authenticator
import com.linku.domain.Result
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.AuthRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.sandbox
import com.linku.domain.service.AuthService
import com.linku.domain.service.ChatService

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userDao: UserDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val chatService: ChatService,
    private val authenticator: Authenticator
) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<Unit> =
        sandbox {
            authService.signIn(email, password)
                .handle { token ->
                    authenticator.update(uid = token.id, token = token.token)
                    val timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3
                    chatService.getMessageAfter(timestamp).handle { messages ->
                        messages.forEach {
                            messageDao.insert(it.toMessage())
                            val cid = it.cid
                            if (conversationDao.getById(cid) == null) {
                                chatService.getById(cid).handle { conversation ->
                                    conversationDao.insert(conversation.toConversation())
                                }
                            }
                        }
                    }
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

    override suspend fun verifyEmailCode(code: String) = sandbox {
        authService.verifyEmailCode(code)
    }

    override suspend fun verifyEmail() = sandbox {
        authService.verifyEmail()
    }

    override suspend fun signOut() {
        userDao.clear()
        conversationDao.clear()
        messageDao.clear()
    }

}