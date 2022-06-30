package com.wzk.domain.service.impl

import com.wzk.domain.entity.Conversation
import com.wzk.domain.entity.Message
import com.wzk.domain.service.ChatService
import com.wzk.wrapper.Resource
import com.wzk.wrapper.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class ChatServiceImpl(
    private val client: HttpClient
) : ChatService {
    override suspend fun getDetail(cid: Int): Resource<Conversation> {
        return try {
            client.get {
                url(ChatService.EndPoints.GetDetail(cid).url)
            }
                .body<Result<Conversation>>()
                .toResource("Couldn't get detail of $cid.")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getAllMessages(cid: Int): Resource<List<Message>> {
        return try {
            client.get {
                url(ChatService.EndPoints.GetAllMessages(cid).url)
            }
                .body<Result<List<Message>>>()
                .toResource("Couldn't get messages of $cid.")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.message ?: "Unknown Error")
        }
    }

    override suspend fun getUnreadMessages(cid: Int, uid: Int): Resource<List<Message>> {
        return try {
            client.get {
                url(ChatService.EndPoints.GetUnreadMessages(cid, uid).url)
            }
                .body<Result<List<Message>>>()
                .toResource("Couldn't get unread messages of $uid in $cid")
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Failure(e.message ?: "Unknown Error")
        }
    }
}