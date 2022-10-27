package com.linku.data.repository

import android.content.Context
import android.net.Uri
import com.linku.data.R
import com.linku.domain.auth.Authenticator
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.AuthRepository
import com.linku.domain.repository.AuthRepository.AfterSignInBehaviour
import com.linku.domain.repository.FileRepository
import com.linku.domain.repository.FileResource
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.AuthService
import com.linku.domain.service.ConversationService
import com.linku.domain.service.MessageService
import com.linku.domain.service.ProfileService
import com.linku.domain.wrapper.Resource
import com.linku.domain.wrapper.resultOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val profileService: ProfileService,
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val authenticator: Authenticator,
    private val fileRepository: FileRepository,
    @ApplicationContext private val context: Context
) : AuthRepository {
    override fun signIn(
        email: String,
        password: String,
        behaviour: AfterSignInBehaviour
    ): Flow<AuthRepository.SignInState> = channelFlow {
        trySend(AuthRepository.SignInState.Start)
        suspend fun makeBehaviour() {
            when (behaviour) {
                AfterSignInBehaviour.DoNothing -> {}
                is AfterSignInBehaviour.SyncUnreadMessages -> {
                    trySend(AuthRepository.SignInState.Syncing)
                    launch(Dispatchers.IO) {
                        val timestamp: Long = messageDao.getLatestMessage()?.timestamp
                            ?: (System.currentTimeMillis() - behaviour.duration)
                        resultOf {
                            messageService.getMessageAfter(timestamp)
                        }
                            .onSuccess { messages ->
                                messages.sortedBy { it.cid }.forEach { message ->
                                    if (messageDao.getById(message.id) == null) {
                                        messageDao.insert(message.toMessage())
                                    }
                                    if (conversationDao.getById(message.cid) == null) {
                                        launch {
                                            resultOf {
                                                conversationService
                                                    .getConversationById(message.cid)
                                            }
                                                .onSuccess {
                                                    conversationDao.insert(it.toConversation())
                                                }
                                        }
                                    }
                                }
                            }
                    }

                }
            }

        }
        launch {
            resultOf {
                authService.signIn(email, password)
            }
                .onSuccess { token ->
                    authenticator.update(token.id, token.token)
                    makeBehaviour()
                    trySend(AuthRepository.SignInState.Completed)
                }
                .onFailure {
                    trySend(AuthRepository.SignInState.Failed(it.message))
                }
        }
    }.catch {
        it.printStackTrace()
        emit(AuthRepository.SignInState.Failed(it.message))
    }

    override suspend fun signUp(
        email: String, password: String, name: String, realName: String?
    ): Result<Unit> = resultOf { authService.signUp(email, password, name, realName) }


    override suspend fun signOut() {
        userDao.clear()
        conversationDao.clear()
        messageDao.clear()
    }

    override suspend fun verifyEmailCode(code: String): Result<Unit> =
        resultOf { authService.verifyEmailCode(code) }


    override suspend fun verifyEmail(): Result<Unit> = resultOf { authService.verifyEmail() }

    override fun uploadAvatar(uri: Uri): Flow<Resource<Unit>> = channelFlow {
        fileRepository.uploadImage(uri, fixedFormat = "png")
            .onEach { resource ->
                when (resource) {
                    FileResource.Loading -> {
                        trySend(Resource.Loading)
                    }

                    FileResource.FileCannotFoundError -> {
                        val msg = context.getString(R.string.error_file_cannot_found)
                        trySend(Resource.Failure(msg))
                    }

                    FileResource.NullUriError -> {
                        val msg = context.getString(R.string.error_null_uri)
                        trySend(Resource.Failure(msg))
                    }

                    is FileResource.OtherError -> {
                        val defaultMsg = context.getString(R.string.error_unknown)
                        trySend(Resource.Failure(resource.message ?: defaultMsg))
                    }

                    is FileResource.Success -> {
                        launch {
                            resultOf { profileService.editAvatar(resource.data.remoteUrl) }
                                .onSuccess { trySend(Resource.Success(Unit)) }
                                .onFailure { trySend(Resource.Failure(it.message)) }
                        }
                    }
                }
            }
            .launchIn(this)
    }
}


