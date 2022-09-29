package com.linku.im.di

import com.linku.data.repository.AuthRepositoryImpl
import com.linku.data.repository.ConversationRepositoryImpl
import com.linku.data.repository.MessageRepositoryImpl
import com.linku.data.repository.SessionRepositoryImpl
import com.linku.data.repository.UserRepositoryImpl
import com.linku.data.service.SessionServiceImpl
import com.linku.domain.repository.AuthRepository
import com.linku.domain.repository.ConversationRepository
import com.linku.domain.repository.MessageRepository
import com.linku.domain.repository.SessionRepository
import com.linku.domain.repository.UserRepository
import com.linku.domain.service.SessionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        messageRepositoryImpl: MessageRepositoryImpl
    ): MessageRepository

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        conversationRepositoryImpl: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSessionService(sessionServiceImpl: SessionServiceImpl): SessionService
}