package com.linku.im.screen.main

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.domain.Authenticator
import com.linku.domain.Resource
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.TextMessage
import com.linku.im.R
import com.linku.im.applicationContext
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val conversationUseCases: ConversationUseCases,
) : BaseViewModel<MainState, MainEvent>(MainState()) {
    init {
        Authenticator.observeCurrent
            .onEach {
                if (it != null) onEvent(MainEvent.GetConversations)
                else getAllConversationsJob?.cancel()
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.CreateConversation -> {}
            MainEvent.GetConversations -> getAllConversations()
        }
    }

    private var getAllConversationsJob: Job? = null
    private fun getAllConversations() {
        conversationUseCases.observeConversations()
            .onEach { conversations ->
                _state.value = state.value.copy(
                    conversations = conversations
                        .filter { it.type == Conversation.TYPE_GROUP }
                        .map { it.toMainUI() },
                    contracts = conversations
                        .filter { it.type == Conversation.TYPE_PM }
                        .map { it.toMainUI() },
                    loading = false
                )
                getAllConversationsJob?.cancel()
                getAllConversationsJob = viewModelScope.launch {
                    conversations.forEach { conversation ->
                        conversationUseCases.observeLatestContent(conversation.id)
                            .collectLatest { message ->
                                val oldConversations = state.value.conversations.toMutableList()
                                val oldContracts = state.value.contracts.toMutableList()
                                val oldConversation = oldConversations.find { it.id == message.cid }
                                val oldContract = oldContracts.find { it.id == message.cid }
                                if (oldConversation != null) {
                                    oldConversations.remove(oldConversation)
                                    val copy = oldConversation.copy(
                                        content = when (message.toReadable()) {
                                            is TextMessage -> message.content
                                            is ImageMessage -> applicationContext.getString(R.string.image_message)
                                            is GraphicsMessage -> applicationContext.getString(R.string.graphics_message)
                                            else -> applicationContext.getString(R.string.unknown_message_type)
                                        }
                                    )
                                    oldConversations.add(copy)
                                } else if (oldContract != null) {
                                    oldContracts.remove(oldContract)
                                    val copy = oldContract.copy(
                                        content = when (message.toReadable()) {
                                            is TextMessage -> message.content
                                            is ImageMessage -> applicationContext.getString(R.string.image_message)
                                            is GraphicsMessage -> applicationContext.getString(R.string.graphics_message)
                                            else -> applicationContext.getString(R.string.unknown_message_type)
                                        }
                                    )
                                    oldConversations.add(copy)
                                } else {
                                    // TODO
                                }
                                _state.value = state.value.copy(
                                    conversations = oldConversations,
                                    contracts = oldContracts
                                )
                            }
                    }
                }
            }
            .launchIn(viewModelScope)
        conversationUseCases.fetchConversations()
            .onEach { resource ->
                _state.value = when (resource) {
                    Resource.Loading -> state.value.copy(
                        loading = true
                    )
                    is Resource.Success -> state.value.copy(
                        loading = false
                    )
                    is Resource.Failure -> state.value.copy(
                        loading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}