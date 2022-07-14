package com.linku.im.screen.main

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.OneWordUseCases
import com.linku.domain.Resource
import com.linku.domain.entity.Conversation
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val conversationUseCases: ConversationUseCases,
    private val oneWordUseCases: OneWordUseCases
) : BaseViewModel<MainState, MainEvent>(MainState()) {

    init {
        onEvent(MainEvent.OneWord)
        onEvent(MainEvent.GetConversations)
    }

    override fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.CreateConversation -> {}
            MainEvent.OneWord -> fetchOneWord()
            MainEvent.GetConversations -> getAllLocalConversations()
        }
    }

    private fun getAllLocalConversations() {
        _state.value = state.value.copy(
            conversations = listOf(
                Conversation(
                    id = 1,
                    updatedAt = System.currentTimeMillis(),
                    name = "1000",
                    avatar = "",
                    owner = 1,
                    member = listOf(),
                    description = ""
                )
            ),
            loading = false
        )
        conversationUseCases.observeConversationsUseCase()
            .onEach {
                delay(2000)
                _state.value = state.value.copy(
                    conversations = it,
                    loading = false
                )
            }
        //.launchIn(viewModelScope)
    }

    private fun fetchOneWord() {
        oneWordUseCases.neteaseUseCase()
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> state.value.copy(
                        drawerTitle = null
                    )
                    is Resource.Success -> state.value.copy(
                        drawerTitle = resource.data
                    )
                    is Resource.Failure -> state.value.copy(
                        drawerTitle = resource.message
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}