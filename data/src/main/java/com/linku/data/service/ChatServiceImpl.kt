package com.linku.data.service

import com.linku.domain.BuildConfig
import com.linku.domain.Result
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.domain.sandbox
import com.linku.domain.service.ChatService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class ChatServiceImpl(
    private val client: HttpClient
) : ChatService {
    private suspend inline fun <reified R> execute(endPoint: ChatService.EndPoints): R {
        return client.request(BuildConfig.BASE_URL) {
            method = endPoint.method
            url {
                path(*endPoint.path)
                endPoint.params.forEach { (k, v) ->
                    v?.also { parameters.append(k, it) }
                }
            }
        }.also {
            it.bodyAsText().also { println("CSSS:$it") }
        }.body()
    }

    override suspend fun sendTextMessage(cid: Int, content: String) = sandbox<Unit> {
        val endPoint = ChatService.EndPoints.SendMessage(cid, content)
        execute(endPoint)
    }

    override suspend fun getDetail(cid: Int) = sandbox<Conversation> {
        val endPoint = ChatService.EndPoints.GetDetail(cid)
        execute(endPoint)
    }


    override suspend fun getAllMessages(cid: Int) = sandbox<List<Message>> {
        val endPoint = ChatService.EndPoints.GetAllMessages(cid)
        execute(endPoint)
    }


    override suspend fun getUnreadMessages(cid: Int, uid: Int): Result<List<Message>> = sandbox {
        val endPoint = ChatService.EndPoints.GetUnreadMessages(cid, uid)
        execute(endPoint)
    }
}