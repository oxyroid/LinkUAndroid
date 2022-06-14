package com.wzk.domain.service

import com.wzk.domain.entity.User
import com.wzk.wrapper.Result
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("/user/{id}")
    suspend fun getById(@Path("id") id: Int): Result<User>

    @GET("/user/register")
    suspend fun register(
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("username") username: String
    ): Result<User>

    @GET("/user/login")
    suspend fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): Result<User>
}

