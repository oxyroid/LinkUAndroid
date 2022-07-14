package com.linku.domain.service

import retrofit2.http.PATCH
import retrofit2.http.Query

interface ProfileService {
    @PATCH("profile/name")
    suspend fun editName(@Query("name") name: String): Result<Unit>

    @PATCH("profile/realname")
    suspend fun editRealName(@Query("realname") name: String): Result<Unit>

    @PATCH("profile/modify")
    suspend fun editPassword(@Query("password") password: String): Result<Unit>

    @PATCH("profile/avatar")
    suspend fun editAvatar(@Query("avatar") avatar: String): Result<Unit>
}