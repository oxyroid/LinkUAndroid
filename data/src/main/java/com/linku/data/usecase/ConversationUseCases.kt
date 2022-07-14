package com.linku.data.usecase

import com.linku.domain.repository.ConversationRepository
import javax.inject.Inject

data class ConversationUseCases @Inject constructor(
    val observeConversationsUseCase: ObserveConversationsUseCase
)

data class ObserveConversationsUseCase(
    private val repository: ConversationRepository
) {
    operator fun invoke() = repository.observeConversations()
}