package com.linku.im.screen.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.extension.TAG
import com.linku.im.extension.debug
import com.linku.domain.service.NotificationService
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases,
    private val notificationService: NotificationService
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {
    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.Initial -> {
                _state.value = _state.value.copy(
                    cid = event.cid,
                )
                viewModelScope.launch {
                    messageUseCases.observeMessagesByCID(event.cid)
                        .onEach {
                            _state.value = state.value.copy(
                                messages = it
                            )
                        }
                        .launchIn(viewModelScope)
                }
            }
            ChatEvent.SendTextMessage -> sendTextMessage()
            is ChatEvent.TextChange -> _state.value = _state.value.copy(text = event.text)
        }
    }

    private fun sendTextMessage() {
        val cid = state.value.cid
        val content = state.value.text
        if (content.isBlank()) return
        viewModelScope.launch {
            messageUseCases.textMessage(cid, content)
                .onEach { resource ->
                    when (resource) {
                        Resource.Loading -> {
                            debug {
                                Log.v(TAG, "Text Message is sending...(content:$content).")
                            }
                            _state.value = state.value.copy(
                                scrollToBottom = eventOf(Unit),
                                text = ""
                            )
                        }
                        is Resource.Success -> {
                            debug {
                                Log.d(
                                    TAG,
                                    "Text Message sent successfully(content:$content)."
                                )
                            }
                            notificationService.onEmit()
                        }
                        is Resource.Failure -> {
                            debug {
                                Log.e(TAG, "Failed to send text message(content:$content).")
                            }
                            _state.value =
                                _state.value.copy(event = eventOf(resource.message))
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

}