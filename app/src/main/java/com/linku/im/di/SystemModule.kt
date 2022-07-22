package com.linku.im.di

import com.linku.domain.service.NotificationService
import com.linku.im.service.NotificationServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemModule {
    @Binds
    @Singleton
    abstract fun bindNotificationService(service: NotificationServiceImpl): NotificationService
}