@file:Suppress("unused")
package com.linku.domain.service

import com.linku.core.wrapper.BackendResult
import retrofit2.http.*

interface ProfileService {

    @FormUrlEncoded
    @POST("profile/name")
    suspend fun editName(@Field("name") name: String): BackendResult<Unit>

    @FormUrlEncoded
    @POST("profile/realname")
    suspend fun editRealName(@Field("realname") name: String): BackendResult<Unit>

    @FormUrlEncoded
    @POST("profile/modify")
    suspend fun editPassword(@Field("password") password: String): BackendResult<Unit>

    @FormUrlEncoded
    @POST("profile/avatar")
    suspend fun editAvatar(@Field("avatar") avatar: String): BackendResult<Unit>
}
