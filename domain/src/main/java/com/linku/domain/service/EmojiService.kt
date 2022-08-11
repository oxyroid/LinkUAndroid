package com.linku.domain.service

import com.linku.domain.Resource
import com.linku.domain.bean.Emoji
import kotlinx.coroutines.flow.Flow

interface EmojiService {
    fun initialize(): Flow<Resource<Unit>>
    fun emojis(): List<Emoji>
}