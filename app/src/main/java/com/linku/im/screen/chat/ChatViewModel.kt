package com.linku.im.screen.chat

import androidx.lifecycle.viewModelScope
import com.linku.domain.Resource
import com.linku.domain.eventOf
import com.linku.domain.usecase.ChatUseCases
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {

    private var job: Job? = null

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> {
                job?.cancel()
                val source = event.source
                _state.value = _state.value.copy(
                    cid = event.cid,
                    messages = source.replayCache.filter { it.cid == state.value.cid }
                )
                job = source.onEach {
                    val newList = _state.value.messages.toMutableList()
                    newList.add(0, it)
                    _state.value = _state.value.copy(
                        messages = newList
                    )
                }.catch {
                    _state.value = _state.value.copy(event = eventOf(it.localizedMessage ?: "?"))
                }.launchIn(viewModelScope)

            }
            ChatEvent.SendTextMessage -> {
                val cid = state.value.cid
                checkNotNull(cid)
                if (state.value.text.isBlank()) return
                chatUseCases.sendTextMessageUseCase(
                    cid = cid,
                    content = state.value.text
                ).onEach { resource ->
                    _state.value = when (resource) {
                        Resource.Loading -> {
                            _state.value.copy(
                                sending = true
                            )
                        }
                        is Resource.Success -> {
                            _state.value.copy(
                                sending = false,
                                text = ""
                            )
                        }
                        is Resource.Failure -> {
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