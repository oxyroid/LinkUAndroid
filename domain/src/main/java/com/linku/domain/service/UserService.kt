package com.linku.domain.service

import com.linku.domain.Result
import com.linku.domain.entity.UserDTO
import retrofit2.http.GET
import retrofit2.http.Path

interface UserService {
    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Int): Result<UserDTO>

    @GET("users/")
    suspend fun getAll(): Result<List<UserDTO>>

}