package com.linku.im.di

import android.content.Context
import com.linku.data.authenticator.PreferenceAuthenticator
import com.linku.data.service.TwitterEmojiService
import com.linku.data.usecase.Configurations
import com.linku.domain.auth.Authenticator
import com.linku.domain.service.EmojiService
import com.linku.im.network.ConnectivityObserver
import com.linku.im.network.NetworkConnectivityObserver
import com.tencent.mmkv.MMKV
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    @Provides
    @Singleton
    fun provideMMKV(): MMKV {
        return MMKV.defaultMMKV()
    }

    @Provides
    @Singleton
    fun provideAuthenticator(
        configurations: Configurations
    ): Authenticator = PreferenceAuthenticator(
        configurations = configurations
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
