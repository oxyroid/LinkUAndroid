package com.linku.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.bean.CachedFile
import com.linku.domain.emitResource
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.AuthRepository
import com.linku.domain.resultOf
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.service.*
import com.linku.fs_android.writeFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject
import kotlin.math.roundToInt

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val profileService: ProfileService,
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
        email: String, password: String
    ): Flow<AuthRepository.SignInState> = channelFlow {
        trySend(AuthRepository.SignInState.Start)
        launch {
            resultOf { authService.signIn(email, password) }
                .onSuccess { token ->
                    authenticator.update(token.id, token.token)
                    launch(Dispatchers.IO) {
                        // Get latest message timestamp from local database at first.
                        // If there is no message at all, 3 days ago instead.
                        // Then fetch messages from server.
                        val timestamp: Long = messageDao.getLatestMessage()?.timestamp
                            ?: (System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 3)
                        resultOf { messageService.getMessageAfter(timestamp) }
                            .onSuccess { messages ->
                                messages.forEachIndexed { index, message ->
                                    launch {
                                        val present: Int = (index / messages.lastIndex.toFloat() * 100).roundToInt()
                                        Log.e("TAG", "signIn: $present")
//                                        if (messageDao.getById(message.id) == null) {
                                        messageDao.insert(message.toMessage())
//                                        }
                                        if (conversationDao.getById(message.cid) == null) {
                                            launch {
                                                resultOf {
                                                    conversationService.getConversationById(message.cid)
                                                }.onSuccess {
                                                    conversationDao.insert(it.toConversation())
                                                }
                                            }
                                        }
                                        trySend(AuthRepository.SignInState.Syncing(present))
                                        if (index == messages.lastIndex) {
                                            trySend(AuthRepository.SignInState.Syncing(100))
                                            trySend(AuthRepository.SignInState.Completed)
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

    private fun uploadImage(uri: Uri?): Flow<Resource<CachedFile>> = flow {
        emit(Resource.Loading)
        if (uri == null) {
            emit(Resource.Failure("Cannot get image"))
            return@flow
        }

        val file = context.writeFs.put(uri)
        file ?: run {
            emit(Resource.Failure("Cannot get image"))
            return@flow
        }
        // FIXME
        val filename = file.name + ".png"
        val part = MultipartBody.Part.createFormData(
            "file", filename, RequestBody.create(MediaType.parse("image"), file)
        )
        resultOf { fileService.upload(part) }
            .onSuccess { emit(Resource.Success(CachedFile(file.toUri(), it))) }
            .onFailure { emitResource(it.message) }
    }

    override fun uploadAvatar(uri: Uri): Flow<Resource<Unit>> = channelFlow {
        uploadImage(uri).onEach { resource ->
            when (resource) {
                is Resource.Success -> {
                    launch {
                        resultOf { profileService.editAvatar(resource.data.remoteUrl) }
                            .onSuccess { trySend(resource.toUnit()) }
                            .onFailure { trySend(Resource.Failure(it.message)) }
                    }
                }
                else -> trySend(resource.toUnit())
            }
        }.launchIn(this)
    }
}
