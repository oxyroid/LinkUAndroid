package com.linku.domain.service

import com.linku.domain.Result
import com.linku.domain.entity.MessageDTO
import retrofit2.http.*

interface MessageService {
    @FormUrlEncoded
    @POST("chats/{cid}/msg")
    suspend fun sendMessage(
        @Path("cid") cid: Int,
        @Field("content") content: String,
        @Field("type") type: String?,
        @Field("uuid") uuid: String
    ): Result<MessageDTO>

    @GET("chats/msg/{mid}")
    suspend fun getMessageById(@Path("mid") mid: Int): Result<MessageDTO>

    @GET("chats/read/{mid}")
    suspend fun read(@Path("mid") mid: Int): Result<Unit>

    @GET("chats/read")
    suspend fun readAll(): Result<Unit>

    @GET("chats/msg")
    suspend fun getUnreadMessages(): Result<List<MessageDTO>>

    @GET("chats/msg/pre")
    suspend fun getMessageAfter(@Query("date") timestamp: Long): Result<List<MessageDTO>>

    @FormUrlEncoded
    @POST("chats/contact/request")
    suspend fun request(
        @Field("content") content: String? = null
    ): Result<Unit>

    @FormUrlEncoded
    @POST("chats/contact/request/{mid}/reject")
    suspend fun rejectRequest(
        @Path("mid") mid: Int,
        @Field("content") content: String? = null
    ): Result<Unit>

    @POST("chats/contact/request/{mid}/accept")
    suspend fun acceptRequest(
        @Path("mid") mid: Int
    ): Result<Unit>


}