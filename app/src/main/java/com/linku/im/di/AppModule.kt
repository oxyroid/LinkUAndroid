package com.linku.im.di

import androidx.room.Room
import com.linku.domain.common.Constants
import com.linku.domain.repository.auth.AuthRepository
import com.linku.domain.repository.auth.AuthRepositoryImpl
import com.linku.domain.repository.chat.ChatRepository
import com.linku.domain.repository.chat.ChatRepositoryImpl
import com.linku.domain.repository.user.UserRepository
import com.linku.domain.repository.user.UserRepositoryImpl
import com.linku.domain.room.ULinkDatabase
import com.linku.domain.service.AuthService
import com.linku.domain.service.ChatService
import com.linku.domain.service.ChatSocketService
import com.linku.domain.service.UserService
import com.linku.domain.service.impl.AuthServiceImpl
import com.linku.domain.service.impl.ChatServiceImpl
import com.linku.domain.service.impl.ChatSocketServiceImpl
import com.linku.domain.service.impl.UserServiceImpl
import com.linku.domain.usecase.*
import com.linku.im.application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDatabase() = Room.databaseBuilder(
        application,
        ULinkDatabase::class.java,
        Constants.DB_NAME
    ).build()

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(Logging) {
                logger = Logger.ANDROID
                level = LogLevel.BODY
            }
            install(WebSockets)
            install(ContentNegotiation) {
                json()
            }
        }
    }

    @Provides
    @Singleton
    fun provideChatService(client: HttpClient): ChatService {
        return ChatServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideChatSocketService(client: HttpClient): ChatSocketService {
        return ChatSocketServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideAuthService(client: HttpClient): AuthService {
        return AuthServiceImpl(client)
    }

    @Provides
    @Singleton
    fun provideUserService(client: HttpClient): UserService {
        return UserServiceImpl(client)
    }

    @Provides
    @Singleton
    fun providesAuthRepository(
        database: ULinkDatabase,
        client: HttpClient
    ): AuthRepository = AuthRepositoryImpl(
        userDao = database.userDao(),
        authService = provideAuthService(client),
    )

    @Provides
    @Singleton
    fun providesUserRepository(
        database: ULinkDatabase,
        client: HttpClient
    ): UserRepository = UserRepositoryImpl(
        userDao = database.userDao(),
        userService = provideUserService(client),
    )

    @Provides
    @Singleton
    fun providesChatRepository(
        client: HttpClient
    ): ChatRepository = ChatRepositoryImpl(
        chatService = provideChatService(client),
    )


    @Provides
    @Singleton
    fun provideAuthUseCases(
        repository: AuthRepository
    ): AuthUseCases {
        return AuthUseCases(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            logoutUseCase = LogoutUseCase
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
        repository: ChatRepository
    ): ChatUseCases {
        return ChatUseCases(
            sendTextMessageUseCase = SendTextMessageUseCase(repository)
        )
    }

}