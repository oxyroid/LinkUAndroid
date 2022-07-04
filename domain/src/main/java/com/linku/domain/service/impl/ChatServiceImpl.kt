package com.linku.domain.service.impl

import com.linku.domain.Result
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Message
import com.linku.domain.sandbox
import com.linku.domain.service.ChatService
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class ChatServiceImpl(
    private val client: HttpClient
) : ChatService {
    override suspend fun sendTextMessage(cid: Int, content: String): Result<Unit> = sandbox {
        client.post {
            url(ChatService.EndPoints.SendMessage(cid, content).url)
        }.body()
    }

    override suspend fun getDetail(cid: Int): Result<Conversation> = sandbox {
        client.get {
            url(ChatService.EndPoints.GetDetail(cid).url)
        }.body()
    }


    override suspend fun getAllMessages(cid: Int): Result<List<Message>> = sandbox {
        client.get {
            url(ChatService.EndPoints.GetAllMessages(cid).url)
        }.body()
    }


    override suspend fun getUnreadMessages(cid: Int, uid: Int): Result<List<Message>> = sandbox {
        client.get {
            url(ChatService.EndPoints.GetUnreadMessages(cid, uid).url)
        }.body()
    }
}