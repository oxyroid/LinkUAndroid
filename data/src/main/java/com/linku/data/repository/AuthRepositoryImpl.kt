package com.linku.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.bean.CachedFile
import com.linku.domain.emitResource
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.AuthRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.AuthService
import com.linku.domain.service.ConversationService
import com.linku.domain.service.FileService
import com.linku.domain.service.MessageService
import com.linku.domain.toResult
import com.linku.fs_android.writeFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val messageService: MessageService,
    private val conversationService: ConversationService,
    private val fileService: FileService,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val userDao: UserDao,
    private val authenticator: Authenticator,
    @ApplicationContext private val context: Context
) : AuthRepository {
    override fun signIn(
        email: String,
        password: String
    ): Flow<AuthRepository.SignInState> = channelFlow {
        trySend(AuthRepository.SignInState.Start)
        launch {
            authService.signIn(email, password)
                .toResult()
                .onSuccess { token ->
                    authenticator.update(token.id, token.token)
                    trySend(AuthRepository.SignInState.Syncing)
                    launch {
                        // Get latest message timestamp from local database at first.
                        // If there is no message at all, 3 days ago instead.
                        // Then fetch messages from server.
                        val timestamp: Long = messageDao.getLatestMessage()?.timestamp
                            ?: (System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 3)
                        messageService.getMessageAfter(timestamp)
                            .toResult()
                            .onSuccess { messages ->
                                messages.forEach { message ->
                                    messageDao.insert(message.toMessage())
                                    if (conversationDao.getById(message.cid) == null) {
                                        launch {
                                            conversationService.getConversationById(
                                                message.cid
                                            )
                                                .toResult()
                                                .onSuccess {
                                                    conversationDao.insert(it.toConversation())
                                                }
                                        }
                                    }
                                }
                            }
                    }
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
        email: String,
        password: String,
        name: String,
        realName: String?
    ): Result<Unit> = authService.signUp(email, password, name, realName).toResult()


    override suspend fun signOut() {
        userDao.clear()
        conversationDao.clear()
        messageDao.clear()
    }

    override suspend fun verifyEmailCode(code: String): Result<Unit> =
        authService.verifyEmailCode(code).toResult()


    override suspend fun verifyEmail(): Result<Unit> =
        authService.verifyEmail().toResult()

    private fun uploadImage(uri: Uri?): Flow<Resource<CachedFile>> = flow {
        if (uri == null) {
            emit(Resource.Failure("Cannot get image"))
            return@flow
        }

        val file = context.writeFs.put(uri)
        file ?: run {
            emit(Resource.Failure("Cannot get image"))
            return@flow
        }
        val filename = file.name
        val part = MultipartBody.Part
            .createFormData(
                "file",
                filename,
                RequestBody.create(MediaType.parse("image"), file)
            )
        fileService.upload(part)
            .toResult()
            .onSuccess {
                val cachedFile = CachedFile(file.toUri(), it)
                emit(Resource.Success(cachedFile))
            }
            .onFailure {
                emitResource(it.message)
            }
    }

    override fun uploadAvatar(uri: Uri): Flow<Resource<Unit>> = uploadImage(uri).map { it.toUnit() }
}