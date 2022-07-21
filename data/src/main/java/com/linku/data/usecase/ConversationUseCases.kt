package com.linku.data.usecase

import com.linku.domain.entity.Conversation
import com.linku.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class ConversationUseCases @Inject constructor(
    val observeConversations: ObserveConversationsUseCase,
    val observeLatestContent: ObserveLatestMessagesUseCase,
    val fetchConversationsUseCase: FetchConversationsUseCase
)

data class ObserveConversationsUseCase(
    private val repository: ConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> = repository.observeConversations()
}


data class ObserveLatestMessagesUseCase(
    private val repository: ConversationRepository
) {
    operator fun invoke(cid: Int) = repository.observeLatestMessages(cid)
}

data class FetchConversationsUseCase(
    private val repository: ConversationRepository
) {
    operator fun invoke() = repository.fetchConversations()
}