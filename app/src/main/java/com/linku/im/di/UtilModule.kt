package com.linku.im.di

import android.content.Context
import com.linku.data.authenticator.PreferenceAuthenticator
import com.linku.data.repository.*
import com.linku.data.service.TwitterEmojiService
import com.linku.data.usecase.*
import com.linku.domain.Authenticator
import com.linku.domain.extension.json
import com.linku.domain.repository.*
import com.linku.domain.service.*
import com.linku.im.network.ConnectivityObserver
import com.linku.im.network.NetworkConnectivityObserver
import com.tencent.mmkv.MMKV
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    @Provides
    @Singleton
    fun provideJson(): Json = json

    @Provides
    @Singleton
    fun provideMMKV(): MMKV = MMKV.defaultMMKV()

    @Provides
    @Singleton
    fun provideAuthenticator(
        sharedPreferenceUseCase: SharedPreferenceUseCase
    ): Authenticator = PreferenceAuthenticator(
        settings = sharedPreferenceUseCase
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