package com.linku.im.screen.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.linku.data.usecase.MessageUseCases
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.im.extension.TAG
import com.linku.im.extension.debug
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageUseCases: MessageUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> {
                _state.value = _state.value.copy(
                    cid = event.cid,
                )
                viewModelScope.launch {
                    // overall.message 是 SharedFlow<Message>, replay = 16
                    messageUseCases.observeMessagesUseCase(this)
                        .onEach {
                            _state.value = state.value.copy(
                                messages = it
                            )
                        }
                        .launchIn(this)
                }
            }
            ChatEvent.SendTextMessage -> {
                val cid = state.value.cid
                val content = state.value.text
                if (content.isBlank()) return
                viewModelScope.launch {
                    val resource = messageUseCases.textMessageUseCase(cid, content)
                    _state.value = when (resource) {
                        Resource.Loading -> {
                            debug {
                                Log.v(TAG, "Text Message is sending...(content:$content).")
                            }
                            _state.value.copy(sending = true)
                        }
                        is Resource.Success -> {
                            debug {
                                Log.d(TAG, "Text Message sent successfully(content:$content).")
                            }
                            _state.value.copy(sending = false, text = "")
                        }
                        is Resource.Failure -> {
                            debug {
                                Log.e(TAG, "Failed to send text message(content:$content).")
                            }
                            _state.value.copy(sending = false, event = eventOf(resource.message))
                        }
                    }
                }
            }
            is ChatEvent.TextChange -> _state.value = _state.value.copy(text = event.text)
        }
    }

}