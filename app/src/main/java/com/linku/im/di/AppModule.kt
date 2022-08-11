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
import com.linku.data.DefaultAuthenticator
import com.linku.data.repository.*
import com.linku.data.service.TwitterEmojiService
import com.linku.data.service.WebSocketServiceImpl
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.extension.json
import com.linku.domain.repository.*
import com.linku.domain.room.LinkUDatabase
import com.linku.domain.service.*
import com.linku.im.BuildConfig
import com.linku.im.Constants
import com.linku.im.R
import com.linku.im.extension.serialization.asConverterFactory
import com.linku.im.network.ConnectivityObserver
import com.linku.im.network.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.linku.domain.common.Constants as DataConstants

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): LinkUDatabase = Room.databaseBuilder(
        context,
        LinkUDatabase::class.java,
        DataConstants.DB_NAME
    ).build()

    @Provides
    @Singleton
    fun provideJson(): Json = json

    @Provides
    @Singleton
    fun provideAuthenticator(
        settingUseCase: SettingUseCase
    ): Authenticator {
        return DefaultAuthenticator(
            settings = settingUseCase
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofitClient(
        json: Json,
        client: OkHttpClient
    ): Retrofit {
        val contentType = DataConstants.MEDIA_TYPE_JSON.toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authenticator: Authenticator
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .writeTimeout(0L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .pingInterval(Duration.ofSeconds(8))
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .also { builder ->
                        authenticator.token?.let { token ->
                            builder.header(DataConstants.HEADER_JWT, token)
                        }
                    }
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()
        return client
    }

    @Provides
    @Singleton
    fun provideChatService(retrofit: Retrofit): ChatService = retrofit.create()

    @Provides
    @Singleton
    fun provideWebSocketService(
        okHttpClient: OkHttpClient,
        chatService: ChatService,
        json: Json
    ): WebSocketService {
        return WebSocketServiceImpl(okHttpClient, json, chatService)
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
        chatService: ChatService,
        authenticator: Authenticator
    ): AuthRepository = AuthRepositoryImpl(
        authService = authService,
        userDao = database.userDao(),
        conversationDao = database.conversationDao(),
        messageDao = database.messageDao(),
        chatService = chatService,
        authenticator = authenticator
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
        socketService: WebSocketService,
        database: LinkUDatabase,
        @ApplicationContext context: Context,
        notificationService: NotificationService,
        fileService: FileService,
        json: Json,
        authenticator: Authenticator
    ): MessageRepository = MessageRepositoryImpl(
        chatService = chatService,
        socketService = socketService,
        messageDao = database.messageDao(),
        conversationDao = database.conversationDao(),
        notificationService = notificationService,
        context = context,
        fileService = fileService,
        json = json,
        authenticator = authenticator
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
    fun provideAuthUseCases(
        repository: AuthRepository,
        authenticator: Authenticator
    ): AuthUseCases {
        return AuthUseCases(
            signIn = SignInUseCase(repository),
            signUp = SignUpUseCase(repository),
            signOut = SignOutUseCase(repository, authenticator),
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
    ): MessageUseCases {
        return MessageUseCases(
            textMessage = TextMessageUseCase(repository),
            initSession = InitSessionUseCase(repository),
            observeAllMessages = ObserveAllMessagesUseCase(repository),
            closeSession = CloseSessionUseCase(repository),
            observeMessages = ObserveMessagesUseCase(repository),
            imageMessage = ImageMessageUseCase(repository),
            graphicsMessage = GraphicsMessageUseCase(repository),
            getMessage = GetMessageUseCase(repository)
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
    fun provideEmojiUseCases(
        emojiService: EmojiService
    ): EmojiUseCases {
        return EmojiUseCases(
            getAll = GetAllUseCase(emojiService),
            initialize = InitializeUseCase(emojiService)
        )
    }

    @Provides
    @Singleton
    fun provideApplicationUseCases(
        @ApplicationContext context: Context
    ): ApplicationUseCases {
        return ApplicationUseCases(
            toast = ToastUseCase(context),
            getString = GetStringUseCase(context),
            getSystemService = GetSystemServiceUseCase(context)
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
    fun provideEmojiPaster(@ApplicationContext context: Context): EmojiService {
        return TwitterEmojiService(context)
    }


    @Provides
    @Singleton
    fun provideSettingUseCases(@ApplicationContext context: Context): SettingUseCase {
        return SettingUseCase(context)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

}