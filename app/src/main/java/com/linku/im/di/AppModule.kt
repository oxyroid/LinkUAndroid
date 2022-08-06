package com.linku.im.di

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.linku.data.repository.*
import com.linku.data.service.ChatSocketServiceImpl
import com.linku.data.service.CommonEmojiPaster
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.extension.json
import com.linku.domain.repository.*
import com.linku.domain.room.LinkUDatabase
import com.linku.domain.service.*
import com.linku.im.BuildConfig
import com.linku.im.Constants
import com.linku.im.R
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
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.linku.domain.common.Constants as DataConstants

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase() = Room.databaseBuilder(
        applicationContext,
        LinkUDatabase::class.java,
        DataConstants.DB_NAME
    ).build()

    @Provides
    @Singleton
    fun provideJson(): Json = json

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofitClient(
        json: Json
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .writeTimeout(0L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .also { builder ->
                        Authenticator.token?.let { token ->
                            builder.header(DataConstants.HEADER_JWT, token)
                        }
                    }
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()
        val contentType = DataConstants.MEDIA_TYPE_JSON.toMediaType()
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
                pingInterval = DataConstants.KTOR_CALL_TIMEOUT
            }
            install(HttpTimeout) {
                socketTimeoutMillis = DataConstants.KTOR_CALL_TIMEOUT
                requestTimeoutMillis = DataConstants.KTOR_CALL_TIMEOUT
                connectTimeoutMillis = DataConstants.KTOR_CALL_TIMEOUT
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
        database: LinkUDatabase,
        chatService: ChatService
    ): AuthRepository = AuthRepositoryImpl(
        authService = authService,
        userDao = database.userDao(),
        conversationDao = database.conversationDao(),
        messageDao = database.messageDao(),
        chatService = chatService
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
        @ApplicationContext context: Context,
        notificationService: NotificationService,
        fileService: FileService,
        json: Json
    ): MessageRepository = MessageRepositoryImpl(
        chatService = chatService,
        socketService = socketService,
        messageDao = database.messageDao(),
        conversationDao = database.conversationDao(),
        notificationService = notificationService,
        context = context,
        fileService = fileService,
        json = json
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
        repository: MessageRepository,
        fileRepository: FileRepository
    ): MessageUseCases {
        return MessageUseCases(
            textMessage = TextMessageUseCase(repository),
            initSession = InitSessionUseCase(repository),
            observeAllMessages = ObserveAllMessagesUseCase(repository),
            closeSession = CloseSessionUseCase(repository),
            observeMessages = ObserveMessagesUseCase(repository),
            imageMessage = ImageMessageUseCase(repository, fileRepository),
            graphicsMessage = GraphicsMessageUseCase(repository, fileRepository)
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
    fun provideEmojiUseCases(
        emojiPaster: EmojiPaster
    ): EmojiUseCases {
        return EmojiUseCases(
            getAll = GetAllUseCase(emojiPaster),
            initialize = InitializeUseCase(emojiPaster)
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

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManagerCompat {
        val manager = NotificationManagerCompat.from(context)
        val channel = NotificationChannel(
            Constants.NOTIFICATION_ID,
            "Messages",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(channel)
        return manager
    }


    @Provides
    @Singleton
    fun provideNotificationBuilder(@ApplicationContext context: Context): NotificationCompat.Builder =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_ID)
            .setContentTitle("Welcome")
            .setContentText("This is content text")
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


    @Provides
    @Singleton
    fun provideEmojiPaster(@ApplicationContext context: Context): EmojiPaster {
        return CommonEmojiPaster(context)
    }

}