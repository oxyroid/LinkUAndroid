@file:Suppress("unused")

package com.linku.im.di

import com.linku.data.service.system.SensorServiceImpl
import com.linku.domain.service.NotificationService
import com.linku.domain.service.system.SensorService
import com.linku.im.network.NotificationServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    @Singleton
    abstract fun bindNotificationService(service: NotificationServiceImpl): NotificationService

    @Binds
    @Singleton
    abstract fun bindSensorService(service: SensorServiceImpl): SensorService
}
