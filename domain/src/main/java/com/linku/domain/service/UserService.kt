package com.linku.domain.service

import com.linku.core.wrapper.BackendResult
import com.linku.domain.entity.UserDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {
    @GET("users/{id}")
    suspend fun getById(@Path("id") id: Int): BackendResult<UserDTO>

    @GET("users/")
    suspend fun getAll(): BackendResult<List<UserDTO>>

    @GET("users/search")
    suspend fun query(
        @Query("name") name: String?,
        @Query("email") email: String?
    ): BackendResult<List<UserDTO>>


}
