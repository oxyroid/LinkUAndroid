package com.linku.im.di

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
                            builder.header("Auth", token)
                        }
                    }
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()
        val contentType = "application/json".toMediaType()
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
                pingInterval = 8000L
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 8000L
                requestTimeoutMillis = 8000L
                connectTimeoutMillis = 8000L
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
        database: LinkUDatabase
    ): MessageRepository = MessageRepositoryImpl(
        chatService = chatService,
        socketService = socketService,
        messageDao = database.messageDao(),
        conversationDao = database.conversationDao()
    )

    @Provides
    @Singleton
    fun provideConversationRepository(
        database: LinkUDatabase
    ): ConversationRepository = ConversationRepositoryImpl(
        conversationDao = database.conversationDao()
    )

    @Provides
    @Singleton
    fun provideAuthUseCases(
        repository: AuthRepository
    ): AuthUseCases {
        return AuthUseCases(
            signInUseCase = SignInUseCase(repository),
            signUpUseCase = SignUpUseCase(repository),
            logoutUseCase = SignOutUseCase(repository),
        )
    }

    @Provides
    @Singleton
    fun provideUserUseCases(
        repository: UserRepository
    ): UserUseCases {
        return UserUseCases(
            findUserUseCase = FindUserUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideChatUseCases(
        repository: MessageRepository
    ): MessageUseCases {
        return MessageUseCases(
            textMessageUseCase = TextMessageUseCase(repository),
            dispatcherUseCase = DispatcherUseCase(repository),
            initSessionUseCase = InitSessionUseCase(repository),
            observeMessagesUseCase = ObserveMessagesUseCase(repository),
            closeSessionUseCase = CloseSessionUseCase(repository),
            observeMessagesByCIDUseCase = ObserveMessagesByCidUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideConversationUseCases(
        repository: ConversationRepository
    ): ConversationUseCases {
        return ConversationUseCases(
            observeConversationsUseCase = ObserveConversationsUseCase(repository)
        )
    }

    @Provides
    @Singleton
    fun provideOneWordUseCases(
        service: OneWordService
    ): OneWordUseCases {
        return OneWordUseCases(
            hitokotoUseCase = HitokotoUseCase(service),
            neteaseUseCase = NeteaseUseCase(service)
        )
    }

}