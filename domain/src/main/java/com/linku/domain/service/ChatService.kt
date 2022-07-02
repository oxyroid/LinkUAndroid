package com.linku.domain.service

import com.linku.domain.BuildConfig
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.wrapper.Resource

interface ChatService {
    suspend fun getDetail(cid: Int): Resource<Conversation>
    suspend fun getAllMessages(cid: Int): Resource<List<Message>>
    suspend fun getUnreadMessages(cid: Int, uid: Int): Resource<List<Message>>

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/chat"
    }

    sealed class EndPoints(val url: String) {
        data class GetDetail(val cid: Int) : EndPoints("$BASE_URL/$cid/detail")
        data class GetAllMessages(val cid: Int) : EndPoints("$BASE_URL/$cid/messages")
        data class GetUnreadMessages(val cid: Int, val uid: Int) :
            EndPoints("$BASE_URL/$cid/message?type=unread&uid=$uid")
    }
}