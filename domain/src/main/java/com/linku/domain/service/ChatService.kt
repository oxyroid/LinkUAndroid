package com.linku.domain.service

import androidx.annotation.Keep
import com.linku.domain.Result
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import io.ktor.http.*

@Keep
interface ChatService {
    suspend fun sendTextMessage(cid: Int, content: String): Result<Unit>
    suspend fun getDetail(cid: Int): Result<Conversation>
    suspend fun subscribe(): Result<Unit>
    suspend fun getAllMessages(cid: Int): Result<List<Message>>
    suspend fun getUnreadMessages(cid: Int, uid: Int): Result<List<Message>>

    sealed class EndPoints(
        override val method: HttpMethod,
        override val params: Map<String, String?> = emptyMap(),
        override vararg val path: String = emptyArray()
    ) : HttpEndPoints(method, params, *path) {
        data class SendMessage(
            val cid: Int,
            val content: String
        ) : EndPoints(
            method = HttpMethod.Post,
            path = arrayOf("chat", "sendMsg"),
            params = mapOf(
                "cid" to cid.toString(),
                "content" to content
            )
        )

        data class GetDetail(val cid: Int) : EndPoints(
            method = HttpMethod.Get,
            path = arrayOf("chat", cid.toString(), "detail")
        )

        data class GetAllMessages(val cid: Int) : EndPoints(
            method = HttpMethod.Get,
            path = arrayOf("chat", cid.toString(), "messages")
        )

        data class GetUnreadMessages(val cid: Int, val uid: Int) :
            EndPoints(
                method = HttpMethod.Get,
                path = arrayOf("chat", cid.toString(), "message"),
                params = mapOf(
                    "type" to "unread",
                    "uid" to uid.toString()
                )
            )

        object Subscribe : EndPoints(
            method = HttpMethod.Get,
            path = arrayOf("chat", "init")
        )
    }
}