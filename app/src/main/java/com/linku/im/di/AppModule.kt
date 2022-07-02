package com.linku.im.di

import androidx.room.Room
import com.linku.domain.LocalSharedPreference
import com.linku.domain.common.Constants
import com.linku.domain.repository.user.UserRepository
import com.linku.domain.repository.user.UserRepositoryImpl
import com.linku.domain.room.ULinkDatabase
import com.linku.domain.service.ChatService
import com.linku.domain.service.ChatSocketService
import com.linku.domain.service.UserService
import com.linku.domain.service.impl.ChatServiceImpl
import com.linku.domain.service.impl.ChatSocketServiceImpl
import com.linku.domain.service.impl.UserServiceImpl
import com.linku.domain.usecase.FindUserUseCase
import com.linku.domain.usecase.LoginUseCase
import com.linku.domain.usecase.RegisterUseCase
import com.linku.domain.usecase.UserUseCases
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
        application, ULinkDatabase::class.java, Constants.DB_NAME
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
    fun provideLocalSharedPreference(database: ULinkDatabase) =
        LocalSharedPreference(database.userDao())

    @Provides
    @Singleton
    fun provideUserService(client: HttpClient): UserService {
        return UserServiceImpl(client)
    }

    @Provides
    @Singleton
    fun providesUserRepository(
        database: ULinkDatabase,
        client: HttpClient,
        localSharedPreference: LocalSharedPreference
    ): UserRepository = UserRepositoryImpl(
        userDao = database.userDao(),
        userService = provideUserService(client),
        sharedPreference = localSharedPreference
    )

    @Provides
    @Singleton
    fun provideUserUseCases(
        repository: UserRepository
    ): UserUseCases {
        return UserUseCases(
            loginUseCase = LoginUseCase(repository),
            registerUseCase = RegisterUseCase(repository),
            findUserUseCase = FindUserUseCase(repository)
        )
    }

}