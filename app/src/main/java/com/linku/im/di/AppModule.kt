package com.linku.im.di

import android.content.Context
import com.linku.data.DefaultAuthenticator
import com.linku.data.repository.*
import com.linku.data.service.TwitterEmojiService
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.extension.json
import com.linku.domain.repository.*
import com.linku.domain.service.*
import com.linku.im.network.ConnectivityObserver
import com.linku.im.network.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideJson(): Json = json

    @Provides
    @Singleton
    fun provideAuthenticator(
        settingUseCase: SettingUseCase
    ): Authenticator = DefaultAuthenticator(
        settings = settingUseCase
    )

    @Provides
    @Singleton
    fun provideEmojiPaster(@ApplicationContext context: Context): EmojiService =
        TwitterEmojiService(context)

    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver =
        NetworkConnectivityObserver(context)

}