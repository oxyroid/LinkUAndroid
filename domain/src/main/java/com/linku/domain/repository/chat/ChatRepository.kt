package com.linku.domain.repository.chat

import com.linku.domain.Result

interface ChatRepository {
    suspend fun sendTextMessage(cid: Int, content: String): Result<Unit>
}