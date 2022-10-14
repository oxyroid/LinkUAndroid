package com.linku.data.di

import com.linku.data.BuildConfig
import com.linku.data.ktx.serialization.asConverterFactory
import com.linku.domain.Authenticator
import com.linku.domain.common.Constants
import com.linku.domain.service.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.create
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofitClient(
        json: Json,
        client: OkHttpClient
    ): Retrofit {
        val contentType = MediaType.get("application/json")
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authenticator: Authenticator
    ): OkHttpClient {
        val client = OkHttpClient.Builder()
            .writeTimeout(0L, TimeUnit.MINUTES)
            .readTimeout(1L, TimeUnit.MINUTES)
            .pingInterval(Duration.ofSeconds(8))
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .also { builder ->
                        authenticator.token?.let { token ->
                            builder.header(Constants.HEADER_JWT, token)
                        }
                    }
                    .method(original.method(), original.body())
                    .build()
                chain.proceed(request)
            }
            .build()
        return client
    }

    @Provides
    @Singleton
    fun provideConversationService(retrofit: Retrofit): ConversationService = retrofit.create()

    @Provides
    @Singleton
    fun provideMessageService(retrofit: Retrofit): MessageService = retrofit.create()

    @Provides
    @Singleton
    fun provideAuthService(retrofit: Retrofit): AuthService = retrofit.create()

    @Provides
    @Singleton
    fun provideUserService(retrofit: Retrofit): UserService = retrofit.create()

    @Provides
    @Singleton
    fun provideProfileService(retrofit: Retrofit): ProfileService = retrofit.create()

    @Provides
    @Singleton
    fun provideFileService(retrofit: Retrofit): FileService = retrofit.create()

}