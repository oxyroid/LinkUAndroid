package com.wzk.oss.di

import androidx.room.Room
import com.wzk.domain.LocalSharedPreference
import com.wzk.domain.common.Constants
import com.wzk.domain.repository.user.UserRepository
import com.wzk.domain.repository.user.UserRepositoryImpl
import com.wzk.domain.room.ULinkDatabase
import com.wzk.domain.service.ChatService
import com.wzk.domain.service.ChatSocketService
import com.wzk.domain.service.UserService
import com.wzk.domain.service.impl.ChatServiceImpl
import com.wzk.domain.service.impl.ChatSocketServiceImpl
import com.wzk.domain.service.impl.UserServiceImpl
import com.wzk.domain.usecase.FindUserUseCase
import com.wzk.domain.usecase.LoginUseCase
import com.wzk.domain.usecase.RegisterUseCase
import com.wzk.domain.usecase.UserUseCases
import com.wzk.oss.application
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
            install(Logging)
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