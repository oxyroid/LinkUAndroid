package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.bean.Emoji
import com.linku.domain.service.EmojiService
import kotlinx.coroutines.flow.Flow

data class EmojiUseCases(
    val initialize: InitializeUseCase,
    val getAll: GetAllUseCase
)

data class InitializeUseCase(
    private val emojiService: EmojiService
) {
    operator fun invoke(): Flow<Resource<Unit>> = emojiService.initialize()
}

data class GetAllUseCase(
    private val emojiService: EmojiService
) {
    operator fun invoke(): List<Emoji> = emojiService.emojis()
}