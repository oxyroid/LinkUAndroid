package com.linku.im.screen.main

import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ApplicationUseCases
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Resource
import com.linku.domain.entity.Conversation
import com.linku.domain.entity.GraphicsMessage
import com.linku.domain.entity.ImageMessage
import com.linku.domain.entity.TextMessage
import com.linku.im.R
import com.linku.im.network.ConnectivityObserver
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
    private val applicationUseCases: ApplicationUseCases,
    private val messageUseCases: MessageUseCases,
    connectivityObserver: ConnectivityObserver
) : BaseViewModel<MainState, MainEvent>(MainState()) {

    init {
        connectivityObserver.observe()
            .onEach { state ->
                when (state) {
                    ConnectivityObserver.State.Available -> {
                        onEvent(MainEvent.GetConversations)
                    }
                    ConnectivityObserver.State.Unavailable -> {}
                    ConnectivityObserver.State.Losing -> {
                        getAllConversationsJob?.cancel()
                    }
                    ConnectivityObserver.State.Lost -> {}
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: MainEvent) {
        when (event) {
            is MainEvent.CreateConversation -> {

            }
            MainEvent.GetConversations -> getAllConversations()
            MainEvent.StartToCreateConversation -> {}
        }
    }

    private var getAllConversationsJob: Job? = null
    private fun getAllConversations() {
        conversationUseCases.observeConversations()
            .onEach { conversations ->
                writable = readable.copy(
                    conversations = conversations
                        .filter { it.type == Conversation.Type.GROUP }
                        .map { it.toMainUI() },
                    contracts = conversations
                        .filter { it.type == Conversation.Type.PM }
                        .map { it.toMainUI() },
                    loading = false
                )
                getAllConversationsJob?.cancel()
                getAllConversationsJob = viewModelScope.launch {
                    conversations.forEach { conversation ->
                        messageUseCases.observeLatestMessage(conversation.id)
                            .collectLatest { message ->
                                val oldConversations = readable.conversations.toMutableList()
                                val oldContracts = readable.contracts.toMutableList()
                                val oldConversation = oldConversations.find { it.id == message.cid }
                                val oldContract = oldContracts.find { it.id == message.cid }
                                if (oldConversation != null) {
                                    oldConversations.remove(oldConversation)
                                    val copy = oldConversation.copy(
                                        content = when (message) {
                                            is TextMessage -> message.text
                                            is ImageMessage -> applicationUseCases.getString(R.string.image_message)
                                            is GraphicsMessage -> applicationUseCases.getString(R.string.graphics_message)
                                            else -> applicationUseCases.getString(R.string.unknown_message_type)
                                        }
                                    )
                                    oldConversations.add(copy)
                                } else if (oldContract != null) {
                                    oldContracts.remove(oldContract)
                                    val copy = oldContract.copy(
                                        content = when (message) {
                                            is TextMessage -> message.content
                                            is ImageMessage -> applicationUseCases.getString(R.string.image_message)
                                            is GraphicsMessage -> applicationUseCases.getString(R.string.graphics_message)
                                            else -> applicationUseCases.getString(R.string.unknown_message_type)
                                        }
                                    )
                                    oldContracts.add(copy)
                                } else {
                                    // TODO
                                }
                                writable = readable.copy(
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
                writable = when (resource) {
                    Resource.Loading -> readable.copy(
                        loading = true
                    )
                    is Resource.Success -> readable.copy(
                        loading = false
                    )
                    is Resource.Failure -> readable.copy(
                        loading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}