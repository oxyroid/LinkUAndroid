@file:Suppress("unused")

package com.linku.im.di

import com.linku.data.authenticator.PreferenceAuthenticator
import com.linku.data.service.system.EmojiServiceImpl
import com.linku.data.service.system.SensorServiceImpl
import com.linku.domain.auth.Authenticator
import com.linku.domain.service.system.EmojiService
import com.linku.domain.service.system.NotificationService
import com.linku.domain.service.system.SensorService
import com.linku.im.network.ConnectivityObserver
import com.linku.im.network.NetworkConnectivityObserver
import com.linku.im.network.NotificationServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindNotificationService(service: NotificationServiceImpl): NotificationService

    @Binds
    @Singleton
    abstract fun bindSensorService(service: SensorServiceImpl): SensorService

    @Binds
    @Singleton
    abstract fun bindEmojiService(service: EmojiServiceImpl): EmojiService

    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(observer: NetworkConnectivityObserver): ConnectivityObserver

    @Binds
    @Singleton
    abstract fun bindAuthenticator(authenticator: PreferenceAuthenticator): Authenticator
}
