package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.bean.Emoji
import com.linku.domain.service.EmojiPaster
import kotlinx.coroutines.flow.Flow

data class EmojiUseCases(
    val initialize: InitializeUseCase,
    val getAll: GetAllUseCase
)

data class InitializeUseCase(
    private val emojiPaster: EmojiPaster
) {
    operator fun invoke(): Flow<Resource<Unit>> = emojiPaster.initialize()
}

data class GetAllUseCase(
    private val emojiPaster: EmojiPaster
) {
    operator fun invoke(): List<Emoji> = emojiPaster.emojis()
}