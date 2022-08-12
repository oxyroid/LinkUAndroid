package com.linku.domain.service

import com.linku.domain.Result
import retrofit2.http.*

interface ProfileService {

    @FormUrlEncoded
    @POST("profile/name")
    suspend fun editName(@Field("name") name: String): Result<Unit>

    @FormUrlEncoded
    @POST("profile/realname")
    suspend fun editRealName(@Field("realname") name: String): Result<Unit>

    @FormUrlEncoded
    @POST("profile/modify")
    suspend fun editPassword(@Field("password") password: String): Result<Unit>

    @FormUrlEncoded
    @POST("profile/avatar")
    suspend fun editAvatar(@Field("avatar") avatar: String): Result<Unit>
}