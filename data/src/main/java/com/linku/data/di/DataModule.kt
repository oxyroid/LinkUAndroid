@file:Suppress("unused")

package com.linku.data.di

import com.linku.data.repository.*
import com.linku.data.service.api.SessionServiceImpl
import com.linku.domain.repository.*
import com.linku.domain.service.api.SessionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    @Singleton
    fun bindAuthRepository(
        repository: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    fun bindUserRepository(
        repository: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    fun bindMessageRepository(
        repository: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    fun bindConversationRepository(
        repository: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    @Singleton
    fun bindSessionRepository(
        repository: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    fun bindFileRepository(
        repository: FileRepositoryImpl
    ): FileRepository

    @Binds
    @Singleton
    fun bindSessionService(service: SessionServiceImpl): SessionService
}
