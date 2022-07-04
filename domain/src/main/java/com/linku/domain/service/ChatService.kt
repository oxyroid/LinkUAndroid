package com.linku.domain.service

import com.linku.domain.BuildConfig
import com.linku.domain.Result
import com.linku.domain.common.buildUrl
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message

interface ChatService {
    suspend fun sendTextMessage(cid: Int, content: String): Result<Unit>
    suspend fun getDetail(cid: Int): Result<Conversation>
    suspend fun getAllMessages(cid: Int): Result<List<Message>>
    suspend fun getUnreadMessages(cid: Int, uid: Int): Result<List<Message>>

    companion object {
        private const val BASE_URL = "${BuildConfig.BASE_URL}/chat"
    }

    sealed class EndPoints(val url: String) {
        data class SendMessage(val cid: Int, val content: String) : EndPoints(
            buildUrl(BASE_URL) {
                path("sendMsgAll")
                query("cid", cid)
                query("content", content)
            }
        )

        data class GetDetail(val cid: Int) : EndPoints(
            buildUrl(BASE_URL) {
                path(cid)
                path("detail")
            }
        )

        data class GetAllMessages(val cid: Int) : EndPoints(
            buildUrl(BASE_URL) {
                path(cid)
                path("messages")
            }
        )

        data class GetUnreadMessages(val cid: Int, val uid: Int) :
            EndPoints(
                buildUrl(BASE_URL) {
                    path(cid)
                    path("message")
                    query("type", "unread")
                    query("uid", uid)
                }
            )
    }
}