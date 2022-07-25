package com.linku.im.di

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.room.Room
import com.linku.data.repository.*
import com.linku.data.service.ChatSocketServiceImpl
import com.linku.data.usecase.*
import com.linku.domain.Auth
import com.linku.domain.common.Constants
import com.linku.domain.repository.*
import com.linku.domain.room.LinkUDatabase
import com.linku.domain.service.*
import com.linku.im.BuildConfig
import com.linku.im.applicationContext
import com.linku.im.extension.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import java.time.Duration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase() = Room.databaseBuilder(
        applicationContext,
        LinkUDatabase::class.java,
        Constants.DB_NAME
    ).build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofitClient(
        json: Json
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .also { builder ->
                        Auth.token?.let { token ->
                            builder.header(Constants.HEADER_JWT, token)
                        }
                    }
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .callTimeout(Duration.ofMillis(Constants.RETROFIT_CALL_TIMEOUT))
            .build()
        val contentType = Constants.MEDIA_TYPE_JSON.toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.SIMPLE
            }
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
                pingInterval = Constants.KTOR_CALL_TIMEOUT
            }
            install(HttpTimeout) {
                socketTimeoutMillis = Constants.KTOR_CALL_TIMEOUT
                requestTimeoutMillis = Constants.KTOR_CALL_TIMEOUT
                connectTimeoutMillis = Constants.KTOR_CALL_TIMEOUT
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
        }
    }

    @Provides
    @Singleton
    fun provideChatService(retrofit: Retrofit): ChatService = retrofit.create()

    @Provides
    @Singleton
    fun provideChatSocketService(client: HttpClient): ChatSocketService {
        return ChatSocketServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService = retrofit.create()


    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create()

    @Provides
    @Singleton
    fun provideProfileService(retrofit: Retrofit): ProfileService = retrofit.create()

    @Provides
    @Singleton
    fun provideFileService(retrofit: Retrofit): FileService = retrofit.create()

    @Provides
    @Singleton
    fun providesAuthRepository(
        authService: AuthService,
        database: LinkUDatabase
    ): AuthRepository = AuthRepositoryImpl(
        authService = authService,
        userDao = database.userDao(),
        conversationDao = database.conversationDao(),
        messageDao = database.messageDao()
    )

    @Provides
    @Singleton
    fun providesUserRepository(
        database: LinkUDatabase,
        userService: UserService
    ): UserRepository = UserRepositoryImpl(
        userService = userService,
        userDao = database.userDao()
    )

    @Provides
    @Singleton
    fun providesMessageRepository(
        chatService: ChatService,
        socketService: ChatSocketService,
        database: LinkUDatabase,
        notificationService: NotificationService
    ): MessageRepository = MessageRepositoryImpl(
        chatService = chatService,
        socketService = socketService,
        messageDao = database.messageDao(),
        conversationDao = database.conversationDao(),
        notificationService = notificationService
    )

    @Provides
    @Singleton
    fun provideConversationRepository(
        database: LinkUDatabase,
        chatService: ChatService,
        userService: UserService
    ): ConversationRepository = ConversationRepositoryImpl(
        conversationDao = database.conversationDao(),
        chatService = chatService,
        messageDao = database.messageDao(),
        userService = userService
    )

    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext context: Context,
        fileService: FileService
    ): FileRepository = FileRepositoryImpl(
        context = context,
        fileService = fileService
    )

    @Provides
    @Singleton
    fun provideAuthUseCases(
        repository: AuthRepository
    ): AuthUseCases {
        return AuthUseCases(
            signIn = SignInUseCase(repository),
            signUp = SignUpUseCase(repository),
            logout = SignOutUseCase(repository),
            verifiedEmail = VerifiedEmailUseCase(repository),
            verifiedEmailCode = VerifiedEmailCodeUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideUserUseCases(
        repository: UserRepository
    ): UserUseCases {
        return UserUseCases(
            findUser = FindUserUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideChatUseCases(
        repository: MessageRepository
    ): MessageUseCases {
        return MessageUseCases(
            textMessage = TextMessageUseCase(repository),
            initSession = InitSessionUseCase(repository),
            observeMessages = ObserveMessagesUseCase(repository),
            closeSession = CloseSessionUseCase(repository),
            observeMessagesFromConversation = ObserveMessagesFromConversationUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideConversationUseCases(
        repository: ConversationRepository
    ): ConversationUseCases {
        return ConversationUseCases(
            observeConversations = ObserveConversationsUseCase(repository),
            observeConversation = ObserveConversationUseCase(repository),
            observeLatestContent = ObserveLatestMessagesUseCase(repository),
            fetchConversation = FetchConversationUseCase(repository),
            fetchConversations = FetchConversationsUseCase(repository),
            queryConversations = QueryConversationsUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideFileUseCases(
        repository: FileRepository
    ): FileUseCases {
        return FileUseCases(
            upload = UploadUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideSoundPool(): SoundPool {
        val attributes = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_NOTIFICATION)
            .build()
        return SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()
            .also {
                it.setOnLoadCompleteListener { soundPool, sampleId, status ->
                    if (status == 0) {
                        soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
                    }
                }
            }
    }
}