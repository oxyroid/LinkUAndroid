package com.linku.data.repository

import com.linku.domain.Result
import com.linku.domain.repository.ChatRepository
import com.linku.domain.sandbox
import com.linku.domain.service.ChatService

class ChatRepositoryImpl(
    private val chatService: ChatService
) : ChatRepository {
    override suspend fun sendTextMessage(cid: Int, content: String): Result<Unit> = sandbox {
        chatService.sendTextMessage(cid, content)
    }
}