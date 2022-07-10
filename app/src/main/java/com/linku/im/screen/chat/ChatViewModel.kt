package com.linku.im.screen.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.ChatUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.extension.TAG
import com.linku.im.extension.debug
import com.linku.im.overall
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> {
                _state.value = _state.value.copy(
                    cid = event.cid,
                )
                viewModelScope.launch {
                    // overall.message 是 SharedFlow<Message>, replay = 16
                    overall.messages
                        .filter { it.cid == event.cid }
                        .collectLatest { value ->
                            val list = _state.value.messages.toMutableList()
                            list.add(0, value)
                            _state.value = _state.value.copy(
                                messages = list
                            )
                        }
                }
            }
            ChatEvent.SendTextMessage -> {
                val cid = state.value.cid
                val content = state.value.text
                if (content.isBlank()) return
                chatUseCases.sendTextMessageUseCase(
                    cid = cid,
                    content = content
                ).onEach { resource ->
                    _state.value = when (resource) {
                        Resource.Loading -> {
                            debug {
                                Log.v(TAG, "Text Message is sending...(content:$content).")
                            }
                            _state.value.copy(
                                sending = true
                            )
                        }
                        is Resource.Success -> {
                            debug {
                                Log.d(TAG, "Text Message sent successfully(content:$content).")
                            }
                            _state.value.copy(
                                sending = false,
                                text = ""
                            )
                        }
                        is Resource.Failure -> {
                            debug {
                                Log.e(TAG, "Failed to send text message(content:$content).")
                            }
                            _state.value.copy(
                                sending = false,
                                event = eventOf(resource.message)
                            )
                        }
                    }
                }.launchIn(viewModelScope)
            }
            is ChatEvent.TextChange -> _state.value = _state.value.copy(text = event.text)
        }
    }

}