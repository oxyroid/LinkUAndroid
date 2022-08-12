package com.linku.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.linku.data.TAG
import com.linku.data.debug
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.Result
import com.linku.domain.entity.toConversation
import com.linku.domain.repository.AuthRepository
import com.linku.domain.room.dao.ConversationDao
import com.linku.domain.room.dao.MessageDao
import com.linku.domain.room.dao.UserDao
import com.linku.domain.sandbox
import com.linku.domain.service.AuthService
import com.linku.domain.service.ChatService
import com.linku.domain.service.FileService
import com.linku.domain.service.ProfileService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileNotFoundException
import java.util.*

class AuthRepositoryImpl(
    private val authService: AuthService,
    private val userDao: UserDao,
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val chatService: ChatService,
    private val authenticator: Authenticator,
    private val context: Context,
    private val fileService: FileService,
    private val profileService: ProfileService
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

    override fun uploadAvatar(uri: Uri): Flow<Resource<Unit>> = channelFlow {
        trySend(Resource.Loading)
        val uid = authenticator.currentUID
        checkNotNull(uid)
        val uuid = UUID.randomUUID().toString()
        val file = File(context.externalCacheDir, "$uuid.png")
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
        val resolver = context.contentResolver
        try {
            file.outputStream().use {
                resolver.openInputStream(uri).use { stream ->
                    if (stream != null) {
                        stream.copyTo(it)
                        val filename = file.name
                        val part = MultipartBody.Part
                            .createFormData(
                                "file",
                                filename,
                                RequestBody.create(MediaType.parse("image"), file)
                            )
                        launch {
                            fileService.upload(part)
                                .handle { avatar ->
                                    launch {
                                        profileService.editAvatar(avatar)
                                            .handleUnit {
                                                userDao.updateAvatar(uid, avatar)
                                                trySend(Resource.Success(Unit))
                                            }
                                            .catch { message, code ->
                                                trySend(Resource.Failure(message, code))
                                            }
                                    }
                                }
                                .catch { message, code ->
                                    trySend(Resource.Failure(message, code))
                                }
                        }

                    } else {
                        debug { Log.e(TAG, "upload: cannot open stream.") }
                        trySend(Resource.Failure("upload: cannot open stream."))
                    }
                }
            }

        } catch (e: FileNotFoundException) {
            debug { Log.e(TAG, "upload: cannot find file.") }
            trySend(Resource.Failure("upload: cannot open stream."))
            return@channelFlow
        }
    }
}