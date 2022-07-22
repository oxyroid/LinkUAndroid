package com.linku.im.di

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.room.Room
import com.linku.data.repository.AuthRepositoryImpl
import com.linku.data.repository.ConversationRepositoryImpl
import com.linku.data.repository.MessageRepositoryImpl
import com.linku.data.repository.UserRepositoryImpl
import com.linku.data.service.ChatSocketServiceImpl
import com.linku.data.usecase.*
import com.linku.domain.Auth
import com.linku.domain.common.Constants
import com.linku.domain.repository.AuthRepository
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.repository.MessageRepository
import com.linku.domain.repository.UserRepository
import com.linku.domain.room.LinkUDatabase
import com.linku.domain.service.*
import com.linku.im.BuildConfig
import com.linku.im.application
import com.linku.im.extension.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
        application,
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
    fun provideThreePartService(retrofit: Retrofit): OneWordService = retrofit.create()


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
        userDao = database.userDao(),
        userService = userService,
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
        chatService: ChatService
    ): ConversationRepository = ConversationRepositoryImpl(
        conversationDao = database.conversationDao(),
        chatService = chatService,
        messageDao = database.messageDao()
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
            observeMessagesByCID = ObserveMessagesByCidUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideConversationUseCases(
        repository: ConversationRepository
    ): ConversationUseCases {
        return ConversationUseCases(
            observeConversations = ObserveConversationsUseCase(repository),
            observeLatestContent = ObserveLatestMessagesUseCase(repository),
            fetchConversations = FetchConversationsUseCase(repository),
            queryConversations = QueryConversationsUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideOneWordUseCases(
        service: OneWordService
    ): OneWordUseCases {
        return OneWordUseCases(
            hitokoto = HitokotoUseCase(service),
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
            .build().also {
                it.setOnLoadCompleteListener { soundPool, sampleId, status ->
                    if (status == 0) {
                        soundPool.play(sampleId, 1f, 1f, 1, 0, 1f)
                    }
                }
            }
    }

    @Provides
    @Singleton
    fun provideApplicationContext(): Context = application
}