@file:Suppress("unused")

package com.linku.domain.service

import com.linku.core.wrapper.BackendResult
import com.linku.domain.bean.Token
import retrofit2.http.*

interface AuthService {
    @FormUrlEncoded
    @POST("auth/signup")
    suspend fun signUp(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("name") name: String,
        @Field("realname") realName: String?
    ): BackendResult<Unit>

    @FormUrlEncoded
    @POST("auth/signin")
    suspend fun signIn(
        @Field("email") email: String,
        @Field("password") password: String
    ): BackendResult<Token>

    @POST("auth/token")
    suspend fun token(): BackendResult<String>

    @GET("auth/email")
    suspend fun verifyEmail(): BackendResult<Unit>

    @GET("auth/email/{code}")
    suspend fun verifyEmailCode(@Path("code") code: String): BackendResult<Unit>

    @GET("auth/forget")
    suspend fun forgetPassword(): BackendResult<Unit>

    @FormUrlEncoded
    @POST("auth/forget/{code}")
    suspend fun forgetPasswordVerify(@Path("code") code: String): BackendResult<Unit>

    @GET("chats/mqtt")
    suspend fun subscribe(): BackendResult<Unit>

}
