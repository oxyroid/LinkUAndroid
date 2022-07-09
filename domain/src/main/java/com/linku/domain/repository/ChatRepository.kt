package com.linku.domain.repository

import androidx.annotation.Keep
import com.linku.domain.Result

@Keep
interface ChatRepository {
    suspend fun sendTextMessage(cid: Int, content: String): Result<Unit>
}