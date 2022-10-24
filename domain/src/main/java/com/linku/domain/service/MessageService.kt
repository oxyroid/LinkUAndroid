package com.linku.domain.service

import com.linku.domain.wrapper.BackendResult
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
    ): BackendResult<MessageDTO>

    @GET("chats/msg/{mid}")
    suspend fun getMessageById(@Path("mid") mid: Int): BackendResult<MessageDTO>

    @GET("chats/read/{mid}")
    suspend fun read(@Path("mid") mid: Int): BackendResult<Unit>

    @GET("chats/read")
    suspend fun readAll(): BackendResult<Unit>

    @GET("chats/msg")
    suspend fun getUnreadMessages(): BackendResult<List<MessageDTO>>

    @GET("chats/msg/pre")
    suspend fun getMessageAfter(@Query("date") timestamp: Long): BackendResult<List<MessageDTO>>

    @FormUrlEncoded
    @POST("chats/contact/request")
    suspend fun request(
        @Field("content") content: String? = null
    ): BackendResult<Unit>

    @FormUrlEncoded
    @POST("chats/contact/request/{mid}/reject")
    suspend fun rejectRequest(
        @Path("mid") mid: Int,
        @Field("content") content: String? = null
    ): BackendResult<Unit>

    @POST("chats/contact/request/{mid}/accept")
    suspend fun acceptRequest(
        @Path("mid") mid: Int
    ): BackendResult<Unit>


}
