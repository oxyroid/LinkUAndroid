package com.linku.domain.service

import com.linku.domain.Result
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Member
import retrofit2.http.*

interface ChatService {
    @FormUrlEncoded
    @POST("chats/{cid}/msg")
    suspend fun sendMessage(
        @Path("cid") cid: Int,
        @Field("content") content: String,
        @Field("type") type: String
    ): Result<Unit>

    @GET("chats/{cid}")
    suspend fun getById(@Path("cid") cid: Int): Result<Conversation>

    @GET("chats/{cid}/members")
    suspend fun getMembersByCid(@Path("cid") cid: Int): Result<List<Member>>

    @GET("chats/read/{mid}")
    suspend fun readAll(@Path("mid") mid: Int): Result<Unit>

    @GET("chats/mqtt")
    suspend fun subscribe(): Result<Unit>
}