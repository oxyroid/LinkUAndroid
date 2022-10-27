package com.linku.domain.service

import com.linku.domain.entity.ConversationDTO
import com.linku.domain.entity.Member
import com.linku.domain.wrapper.BackendResult
import retrofit2.http.*

interface ConversationService {
    @POST("chats/{cid}/exit")
    suspend fun exitConversation(@Path("cid") cid: Int): BackendResult<Unit>

    @POST("chats/{cid}/disable")
    suspend fun disableConversation(@Path("cid") cid: Int): BackendResult<Unit>

    @FormUrlEncoded
    @POST("chats/invite/{mid}/reject")
    suspend fun rejectConInvitation(
        @Path("mid") mid: Int,
        @Field("content") content: String? = null
    ): BackendResult<Unit>

    @POST("chats/invite/{mid}/accept")
    suspend fun acceptConInvitation(
        @Path("mid") mid: Int
    ): BackendResult<Unit>

    @FormUrlEncoded
    @POST("chats/invite/{cid}")
    suspend fun invite(
        @Path("cid") cid: Int,
        @Field("tid") tid: Int
    ): BackendResult<Unit>

    @GET("chats/{cid}")
    suspend fun getConversationById(@Path("cid") cid: Int): BackendResult<ConversationDTO>

    @GET("chats/{cid}/members")
    suspend fun getMembersByCid(@Path("cid") cid: Int): BackendResult<List<Member>>


    @GET("chats/query")
    suspend fun queryConversations(
        @Query("name") name: String? = null,
        @Query("description") description: String? = null,
    ): BackendResult<List<ConversationDTO>>

    @GET("chats/self")
    suspend fun getConversationsBySelf(
        @Query("type") type: Int? = null,
        @Query("owner") owner: Boolean? = null
    ): BackendResult<List<ConversationDTO>>

}
