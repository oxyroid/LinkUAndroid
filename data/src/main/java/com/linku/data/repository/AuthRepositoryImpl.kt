package com.linku.data.repository

import android.content.Context
import android.net.Uri
import com.linku.core.wrapper.Resource
import com.linku.core.wrapper.resultOf
import com.linku.data.R
import com.linku.domain.auth.Authenticator
import com.linku.domain.repository.AuthRepository
import com.linku.domain.repository.FileRepository
import com.linku.domain.repository.FileResource
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.api.AuthService
import com.linku.domain.service.api.ProfileService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val profileService: ProfileService,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val authenticator: Authenticator,
    private val fileRepository: FileRepository,
    @ApplicationContext private val context: Context
) : AuthRepository {
    override fun signIn(
        email: String,
        password: String
    ): Flow<Resource<Unit>> = channelFlow {
        trySend(Resource.Loading)
        launch {
            resultOf {
                authService.signIn(email, password)
            }
                .onSuccess { token ->
                    authenticator.update(token.id, token.token)
                    trySend(Resource.Success(Unit))
                }
                .onFailure {
                    trySend(Resource.Failure(it.message))
                }
        }
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


