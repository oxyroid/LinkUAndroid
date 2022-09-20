package com.linku.data.usecase

import com.linku.domain.Resource
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Member
import com.linku.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class ConversationUseCases @Inject constructor(
    val observeConversation: ObserveConversationUseCase,
    val observeConversations: ObserveConversationsUseCase,
    val fetchConversation: FetchConversationUseCase,
    val fetchConversations: FetchConversationsUseCase,
    val fetchMembers: FetchMembersUseCase,
    val queryConversations: QueryConversationsUseCase
)

data class ObserveConversationUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(cid: Int): Flow<Conversation> {
        repository.fetchConversation(cid)
        return repository.observeConversation(cid)
    }
}

data class ObserveConversationsUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(): Flow<List<Conversation>> = repository.observeConversations()
}


data class FetchConversationUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(cid: Int): Flow<Resource<Unit>> = repository.fetchConversation(cid)
}

data class FetchMembersUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(cid: Int): Flow<Resource<List<Member>>> = repository.fetchMembers(cid)
}

data class FetchConversationsUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = repository.fetchConversations()
}

data class QueryConversationsUseCase @Inject constructor(
    private val repository: ConversationRepository
) {
    operator fun invoke(name: String?, description: String?): Flow<Resource<List<Conversation>>> =
        repository.queryConversations(name, description)
}