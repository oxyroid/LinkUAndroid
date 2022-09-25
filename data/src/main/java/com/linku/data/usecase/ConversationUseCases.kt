package com.linku.data.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import com.linku.domain.Resource
import com.linku.domain.Strategy
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.Member
import com.linku.domain.repository.ConversationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class ConversationUseCases @Inject constructor(
    val observeConversation: ObserveConversationUseCase,
    val observeConversations: ObserveConversationsUseCase,
    val fetchConversation: FetchConversationUseCase,
    val fetchConversations: FetchConversationsUseCase,
    val fetchMembers: FetchMembersUseCase,
    val queryConversations: QueryConversationsUseCase,
    val pushConversationShort: PushConversationShortCutUseCase
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

data class PushConversationShortCutUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ConversationRepository
) {
    operator fun invoke(cid: Int): Flow<Resource<Unit>> = flow {
        val builder = ShortcutInfoCompat.Builder(context, "shortcut_$cid")
        emit(Resource.Loading)
        runCatching {
            val conversation = repository.findConversation(cid, Strategy.Memory)
            val info = builder
                .setShortLabel(conversation?.name ?: "Conversation#$cid")
                .setIsConversation()
                .setIntent(
                    Intent(
                        Intent.ACTION_VIEW,
                        // Rick Astley - Never Gonna Give You Up (Official Music Video)
                        Uri.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ/")
                    )
                )
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, info)
        }
            .onSuccess { emit(Resource.Success(Unit)) }
            .onFailure { emit(Resource.Failure(it.message)) }
    }
}