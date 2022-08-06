package com.linku.domain.service

import com.linku.domain.Result
import com.linku.domain.entity.ConversationDTO
import com.linku.domain.entity.Member
import com.linku.domain.entity.MessageDTO
import retrofit2.http.*

@Suppress("unused")
interface ChatService {
    @FormUrlEncoded
    @POST("chats/{cid}/msg")
    suspend fun sendMessage(
        @Path("cid") cid: Int,
        @Field("content") content: String,
        @Field("type") type: String?,
        @Field("uuid") uuid: String
    ): Result<MessageDTO>

    @POST("chats/{cid}/exit")
    suspend fun exitConversation(@Path("cid") cid: Int): Result<Unit>

    @POST("chats/{cid}/disable")
    suspend fun disableConversation(@Path("cid") cid: Int): Result<Unit>

    @FormUrlEncoded
    @POST("chats/invite/{mid}/reject")
    suspend fun rejectConInvitation(
        @Path("mid") mid: Int,
        @Field("content") content: String? = null
    ): Result<Unit>

    @POST("chats/invite/{mid}/accept")
    suspend fun acceptConInvitation(
        @Path("mid") mid: Int
    ): Result<Unit>

    @FormUrlEncoded
    @POST("chats/invite/{cid}")
    suspend fun invite(
        @Path("cid") cid: Int,
        @Field("tid") tid: Int
    ): Result<Unit>

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


    @GET("chats/{cid}")
    suspend fun getById(@Path("cid") cid: Int): Result<ConversationDTO>

    @GET("chats/{cid}/members")
    suspend fun getMembersByCid(@Path("cid") cid: Int): Result<List<Member>>

    @GET("chats/read/{mid}")
    suspend fun read(@Path("mid") mid: Int): Result<Unit>

    @GET("chats/read")
    suspend fun readAll(@Path("mid") mid: Int): Result<Unit>

    @GET("chats/msg")
    suspend fun getUnreadMessages(): Result<List<MessageDTO>>


    @GET("chats/query")
    suspend fun queryConversations(
        @Query("name") name: String? = null,
        @Query("description") description: String? = null,
    ): Result<List<ConversationDTO>>

    @GET("chats/msg/pre")
    suspend fun getMessageAfter(@Query("date") timestamp: Long): Result<List<MessageDTO>>

    @GET("chats/mqtt")
    suspend fun subscribe(): Result<Unit>

    @GET("chats/self")
    suspend fun getConversationsBySelf(
        @Query("type") type: Int? = null,
        @Query("owner") owner: Boolean? = null
    ): Result<List<ConversationDTO>>
}