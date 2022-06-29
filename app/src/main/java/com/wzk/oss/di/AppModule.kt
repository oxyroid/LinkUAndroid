package com.wzk.oss.di

import androidx.room.Room
import com.wzk.domain.LocalSharedPreference
import com.wzk.domain.common.Constants
import com.wzk.domain.repository.food.FoodRepository
import com.wzk.domain.repository.food.FoodRepositoryImpl
import com.wzk.domain.repository.food.FoodRepositoryMock
import com.wzk.domain.repository.user.UserRepository
import com.wzk.domain.repository.user.UserRepositoryImpl
import com.wzk.domain.repository.user.UserRepositoryMock
import com.wzk.domain.room.MyDatabase
import com.wzk.domain.service.ChatSocketService
import com.wzk.domain.service.ChatSocketServiceImpl
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
import io.ktor.client.plugins.json.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideDatabase() = Room.databaseBuilder(
        application,
        MyDatabase::class.java,
        Constants.DB_NAME
    ).build()

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(Logging)
            install(WebSockets)
            install(JsonPlugin) {
                serializer = KotlinxSerializer()
            }
        }
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
    fun provideRetrofitService(): Retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideLocalSharedPreference(database: MyDatabase) =
        LocalSharedPreference(database.userDao())

    @Provides
    @Singleton
    fun providesUserRepository(
        database: MyDatabase,
        localSharedPreference: LocalSharedPreference
    ): UserRepository =
        if (Constants.MOCK_MODE) UserRepositoryMock(
            localSharedPreference
        ) else UserRepositoryImpl(
            userDao = database.userDao(),
            userService = provideRetrofitService().create(),
            sharedPreference = localSharedPreference
        )

    @Provides
    @Singleton
    fun providesFoodRepository(
        database: MyDatabase
    ): FoodRepository =
        if (Constants.MOCK_MODE) FoodRepositoryMock()
        else FoodRepositoryImpl(
            foodDao = database.foodDao(),
            foodService = provideRetrofitService().create()
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