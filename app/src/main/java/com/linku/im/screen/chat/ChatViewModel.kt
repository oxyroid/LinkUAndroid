package com.linku.im.screen.chat

import androidx.lifecycle.viewModelScope
import com.linku.domain.Resource
import com.linku.domain.entity.Message
import com.linku.domain.eventOf
import com.linku.data.usecase.ChatUseCases
import com.linku.im.screen.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCases: ChatUseCases
) : BaseViewModel<ChatState, ChatEvent>(ChatState()) {

    private var _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.InitChat -> {
                val source = event.source
                viewModelScope.launch {
                    _messages.emit(source.replayCache)
                    source
                        .filter { it.cid == event.cid }
                        .onEach {
                            val oldList = _messages.replayCache.last().toMutableList()
                            oldList.add(0, it)
                            _messages.emit(oldList)
                        }
                        .launchIn(this)
                }
                _state.value = _state.value.copy(
                    cid = event.cid,
                )
            }
            ChatEvent.SendTextMessage -> {
                val cid = state.value.cid
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

fun main() = runBlocking {
    val sharedFlow = MutableSharedFlow<Int>()
    sharedFlow.emit(1)
    var flow = flow<Int> { }
    coroutineScope {
        flow.collectLatest {
            println(it)
        }
    }
    flow = sharedFlow.filter { true }
    sharedFlow.emit(2)

}