package com.linku.im.screen.chat

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ConversationUseCases
import com.linku.data.usecase.FileUseCases
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.domain.service.NotificationService
import com.linku.im.applicationContext
import com.linku.im.extension.ifTrue
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val conversationUseCases: ConversationUseCases,
    private val messageUseCases: MessageUseCases,
    private val notificationService: NotificationService,
    private val fileUseCases: FileUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {
    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Initial -> {
                _state.value = _state.value.copy(
                    cid = event.cid
                )
                conversationUseCases.observeConversation(event.cid)
                    .onEach { conversation ->
                        _state.value = readable.copy(
                            title = conversation.name,
                            cid = conversation.id,
                            type = conversation.type
                        )
                    }
                    .launchIn(viewModelScope)

                messageUseCases.observeMessagesFromConversation(event.cid)
                    .onEach {
                        _state.value = readable.copy(
                            messages = it,
                            scrollToBottom = (readable.firstVisibleIndex == 0)
                                .ifTrue { eventOf(Unit) }
                                ?: readable.scrollToBottom,
                        )
                    }
                    .launchIn(viewModelScope)

                conversationUseCases.fetchConversation(event.cid)
                    .launchIn(viewModelScope)
            }
            ChatEvent.SendMessage -> {
                when (readable.uri) {
                    null -> sendTextMessage()
                    else -> sendImageMessage()
                }
            }
            is ChatEvent.TextChange -> onTextChange(event.text)
            is ChatEvent.FirstVisibleIndex -> {
                _state.value = readable.copy(
                    firstVisibleIndex = event.index
                )
            }
            ChatEvent.ReadAll -> {

            }
            is ChatEvent.OnFileUriChange -> {
                val uri = event.uri
                val bitmap = if (uri != null) {
                    applicationContext.contentResolver.openInputStream(event.uri).use {
                        BitmapFactory.decodeStream(it)
                    }
                } else null
                _state.value = readable.copy(
                    uri = event.uri,
                    image = bitmap
                )
            }
        }
    }

    private fun onTextChange(value: String) {
        _state.value = readable.copy(text = value)
    }

    private fun sendTextMessage() {
        val cid = readable.cid
        val content = readable.text
        if (content.isBlank()) return
        viewModelScope.launch {
            messageUseCases.textMessage(cid, content)
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            _state.value = readable.copy(
                                scrollToBottom = eventOf(Unit),
                                text = ""
                            )
                        }
                        is Resource.Success -> {
                            notificationService.onEmit()
                        }
                        is Resource.Failure -> {
                            _state.value = readable.copy(event = eventOf(resource.message))
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    private fun sendImageMessage() {
        fileUseCases.upload(readable.uri)
            .onEach { resource ->
                when (resource) {
                    Resource.Loading -> {
                        Toast.makeText(
                            applicationContext,
                            "Uploading...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Resource.Success -> {
                        Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Resource.Failure -> {
                        Toast.makeText(
                            applicationContext,
                            "Failed to upload, ${resource.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

}